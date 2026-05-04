package com.openjar.recipe_service.Service;

import com.openjar.recipe_service.Configuration.RabbitMQConfig;
import com.openjar.recipe_service.DTO.EmailNotificationDto;
import com.openjar.recipe_service.DTO.RecipeRequestDto;
import com.openjar.recipe_service.DTO.RecipeSyncMessage;
import com.openjar.recipe_service.DTO.UserDto;
import com.openjar.recipe_service.Model.Recipe;
import com.openjar.recipe_service.Repository.RecipeRepo;
import lombok.extern.slf4j.Slf4j; // 1. Added Slf4j annotation
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j // 2. This creates the 'log' object automatically via Lombok
public class RecipeService {

    private final RecipeRepo recipeRepo;
    private final WebClient webClient;
    private final RabbitTemplate rabbitTemplate;

    public RecipeService(RecipeRepo recipeRepo, RabbitTemplate rabbitTemplate, WebClient.Builder webClientBuilder) {
        this.recipeRepo = recipeRepo;
        this.rabbitTemplate = rabbitTemplate;
        this.webClient = webClientBuilder.baseUrl("http://localhost:8086").build();
    }

    @Transactional
    public String saveRecipe(RecipeRequestDto recipe, String userId) {
        log.info("Process started: Saving recipe for User ID: {}", userId);

        int rows = recipeRepo.insertRecipe(
                recipe.getRecipeName(),
                recipe.getPrepTime(),
                recipe.getCategory(),
                recipe.getIngredients(),
                recipe.getRecipeInstructions(),
                recipe.getRecipeImageUrl(),
                userId
        );

        if (rows > 0) {
            Long newRecipeId = recipeRepo.getLastInsertId();
            try {
                UserDto ownerProfile = webClient.get()
                        .uri("/api/users/{id}", userId) // Calls localhost:8086/api/users/{id}
                        .retrieve()
                        .bodyToMono(UserDto.class)
                        .block();

                syncToAiProvider(recipe, newRecipeId);

                if (ownerProfile != null && ownerProfile.getUserEmail() != null) {
                    log.info("Successfully fetched profile for: {}", ownerProfile.getUserName());

                    triggerNotification(recipe, ownerProfile);

                    return "Recipe added successfully for " + ownerProfile.getUserName();
                }
            } catch (Exception e) {
                log.error("Failed to fetch user from Route: {}", e.getMessage());
                throw new RuntimeException("User Service communication failed");
            }
        }
        return "Database error: Recipe could not be saved.";
    }

    private void triggerNotification(RecipeRequestDto recipe, UserDto user) {
        Map<String, Object> model = new HashMap<>();
        model.put("userName", user.getUserName());
        model.put("recipeName", recipe.getRecipeName());
        model.put("category", recipe.getCategory());
        model.put("prepTime", recipe.getPrepTime());
        model.put("recipeImageUrl", recipe.getRecipeImageUrl());

        EmailNotificationDto notification = new EmailNotificationDto(
                user.getUserEmail(),
                "Success! '" + recipe.getRecipeName() + "' is now on OpenJar",
                "recipe-created", // The HTML file name
                model
        );

        rabbitTemplate.convertAndSend(
                RabbitMQConfig.EXCHANGE,
                RabbitMQConfig.N_ROUTING_KEY,
                notification
        );

        log.info("Recipe creation email queued for {}", user.getUserEmail());
    }

    public List<Recipe> getAllRecipe() {
        log.info("Fetching all recipes...");
        return recipeRepo.getAllRecipe();
    }

    public Recipe getRecipeById(long id) {
        log.info("Fetching recipe details for ID: {}", id);
        return recipeRepo.getRecipeById(id);
    }

    @Transactional
    public String deleteRecipeById(long recipeId) {
        log.info("Initiating deletion for Recipe ID: {}", recipeId);
        Recipe recipe = recipeRepo.getRecipeById(recipeId);

        if (recipe == null) {
            log.warn("Delete aborted. Recipe ID {} does not exist.", recipeId);
            throw new RuntimeException("Recipe not found");
        }

        int rows = recipeRepo.deleteRecipeById(recipeId);
        if (rows > 0) {
            log.info("Successfully deleted Recipe ID: {}", recipeId);
            return "Recipe deleted successfully";
        }
        return "Recipe was not deleted";
    }

    @Transactional
    public Recipe updateRecipeById(long recipeId, RecipeRequestDto updatedRecipe, String userId) {
        log.info("Updating Recipe ID: {} for User ID: {}", recipeId, userId);

        int rows = recipeRepo.updateRecipe(
                updatedRecipe.getRecipeName(),
                updatedRecipe.getPrepTime(),
                updatedRecipe.getCategory(),
                updatedRecipe.getIngredients(),
                updatedRecipe.getRecipeInstructions(),
                updatedRecipe.getRecipeImageUrl(),
                userId,
                recipeId
        );

        if (rows == 0) {
            log.error("Update failed. Recipe ID {} not found.", recipeId);
            throw new RuntimeException("Recipe not found");
        }

        log.info("Update successful for Recipe ID: {}", recipeId);
        return recipeRepo.getRecipeById(recipeId);


    }

    private void syncToAiProvider(RecipeRequestDto recipeDto, Long newRecipeId) {
        try {
            log.info("Preparing AI Sync for Recipe ID: {}", newRecipeId);

            // 1. Create the Sync Message using the ID we got from LAST_INSERT_ID()
            // This matches the Record we created in the Recommendation Service
            RecipeSyncMessage aiSyncMessage = new RecipeSyncMessage(
                    newRecipeId,
                    recipeDto.getRecipeName(),
                    recipeDto.getCategory(),
                    recipeDto.getIngredients()
            );

            // 2. Send to RabbitMQ
            // We use the direct queue name "recipe.sync.queue"
            // to match the @RabbitListener in your Recommendation Service
            rabbitTemplate.convertAndSend("recipe.sync.queue", aiSyncMessage);

            log.info("Successfully dispatched Recipe [{}] to AI Queue for indexing.", recipeDto.getRecipeName());

        } catch (Exception e) {
            // We catch the error here so the User Service doesn't crash
            // if RabbitMQ is temporarily disconnected.
            log.error("AI Sync failed for Recipe ID {}: {}. Recipe remains saved in DB.",
                    newRecipeId, e.getMessage());
        }
    }
}