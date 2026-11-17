package com.openjar.recipe_service.Service;

import com.openjar.recipe_service.Configuration.RabbitMQConfig;
import com.openjar.recipe_service.Controller.RecipeController.CurrentUser;
import com.openjar.recipe_service.DTO.EmailNotificationDto;
import com.openjar.recipe_service.DTO.RecipeRequestDto;
import com.openjar.recipe_service.DTO.RecipeSyncMessage;
import com.openjar.recipe_service.Model.Recipe;
import com.openjar.recipe_service.Repository.RecipeRepo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class RecipeService {

    private final RecipeRepo recipeRepo;
    private final RabbitTemplate rabbitTemplate;

    public List<Recipe> getAllRecipes() {
        return recipeRepo.findAllByOrderByRecipeIdDesc();
    }

    public List<Recipe> getRecipesByUser(String userId) {
        return recipeRepo.findByUserIdOrderByRecipeIdDesc(userId);
    }

    public Recipe getRecipeById(long id) {
        return recipeRepo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Recipe not found"));
    }

    /**
     * Persists the recipe first; notifications and AI indexing are best-effort side effects
     * that can never roll back or fail the user's request.
     */
    public Recipe saveRecipe(RecipeRequestDto dto, CurrentUser owner) {
        Recipe recipe = new Recipe();
        apply(recipe, dto);
        recipe.setUserId(owner.userId());
        Recipe saved = recipeRepo.save(recipe);
        log.info("Recipe {} created by user {}", saved.getRecipeId(), owner.userId());

        syncToAiProvider(saved);
        notifyOwner(saved, owner);
        return saved;
    }

    @Transactional
    public Recipe updateRecipeById(long recipeId, RecipeRequestDto dto, String userId) {
        Recipe recipe = getOwnedRecipe(recipeId, userId);
        apply(recipe, dto);
        return recipeRepo.save(recipe);
    }

    @Transactional
    public void deleteRecipeById(long recipeId, String userId) {
        Recipe recipe = getOwnedRecipe(recipeId, userId);
        recipeRepo.delete(recipe);
        log.info("Recipe {} deleted by owner {}", recipeId, userId);
    }

    private Recipe getOwnedRecipe(long recipeId, String userId) {
        Recipe recipe = getRecipeById(recipeId);
        if (!recipe.getUserId().equals(userId)) {
            log.warn("User {} tried to modify recipe {} owned by {}", userId, recipeId, recipe.getUserId());
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You can only modify your own recipes");
        }
        return recipe;
    }

    private static void apply(Recipe recipe, RecipeRequestDto dto) {
        recipe.setRecipeName(dto.getRecipeName().trim());
        recipe.setPrepTime(dto.getPrepTime());
        recipe.setCategory(dto.getCategory().trim());
        recipe.setIngredients(dto.getIngredients());
        recipe.setRecipeInstructions(dto.getRecipeInstructions());
        String image = dto.getRecipeImageUrl();
        recipe.setRecipeImageUrl(image == null || image.isBlank() ? null : image.trim());
    }

    private void notifyOwner(Recipe recipe, CurrentUser owner) {
        if (owner.email() == null) {
            return;
        }
        try {
            Map<String, Object> model = new HashMap<>();
            model.put("userName", owner.userName());
            model.put("recipeName", recipe.getRecipeName());
            model.put("category", recipe.getCategory());
            model.put("prepTime", recipe.getPrepTime());
            model.put("recipeImageUrl", recipe.getRecipeImageUrl());

            rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE, RabbitMQConfig.N_ROUTING_KEY,
                    new EmailNotificationDto(owner.email(),
                            "Success! '" + recipe.getRecipeName() + "' is now on OpenJar",
                            "recipe-created", model));
        } catch (Exception e) {
            log.error("Could not queue recipe-created email for recipe {}: {}", recipe.getRecipeId(), e.getMessage());
        }
    }

    private void syncToAiProvider(Recipe recipe) {
        try {
            rabbitTemplate.convertAndSend("recipe.sync.queue", new RecipeSyncMessage(
                    recipe.getRecipeId(), recipe.getRecipeName(), recipe.getCategory(), recipe.getIngredients()));
        } catch (Exception e) {
            log.error("AI sync failed for recipe {}: {}", recipe.getRecipeId(), e.getMessage());
        }
    }
}
