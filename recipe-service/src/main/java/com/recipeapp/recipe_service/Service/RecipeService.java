package com.recipeapp.recipe_service.Service;

import com.recipeapp.recipe_service.Configuration.RabbitMQConfig;
import com.recipeapp.recipe_service.DTO.*;
import com.recipeapp.recipe_service.Model.Recipe;
import com.recipeapp.recipe_service.Repository.RecipeRepo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;

@Service
@Slf4j
public class RecipeService {

    private final RecipeRepo recipeRepo;
    private final WebClient webClient;
    private final RabbitTemplate rabbitTemplate;

    public RecipeService(RecipeRepo recipeRepo, RabbitTemplate rabbitTemplate, WebClient.Builder webClientBuilder) {
        this.recipeRepo = recipeRepo;
        this.rabbitTemplate = rabbitTemplate;
        this.webClient = webClientBuilder.baseUrl("http://localhost:8086").build();
    }

    // Fixed: Added the Long recipeId back to the signature to match your Controller
    @Transactional
    public String saveRecipe(RecipeRequestDto recipeRequest) {
        try {
            int rows = recipeRepo.insertRecipe(
                    recipeRequest.getRecipeName(),
                    recipeRequest.getPrepTime(),
                    recipeRequest.getCategory(),
                    recipeRequest.getIngredients(),
                    recipeRequest.getRecipeInstructions(),
                    recipeRequest.getRecipeImageUrl(),
                    recipeRequest.getUserId()
            );

            log.info("Database insert successful. Rows affected: {}", rows);

            if (rows > 0) {
                log.info("Fetching user details for ID: {}", recipeRequest.getUserId());

                UserDto user = webClient.get()
                        .uri("/api/users/{id}", recipeRequest.getUserId())
                        .retrieve()
                        .bodyToMono(UserDto.class)
                        .block();

                if (user != null && user.getUserEmail() != null) {
                    log.info("User found: {}. Triggering notification...", user.getUserEmail());
                    triggerNotification(recipeRequest.getRecipeName(), user.getUserEmail());
                } else {
                    log.warn("User Service returned null or empty email for ID: {}", recipeRequest.getUserId());
                }
                return "Recipe added successfully";
            }
            return "Recipe cannot be added";
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }


    public List<Recipe> getAllRecipe() {
        return recipeRepo.findAll();
    }

    public Recipe getRecipeById(long recipeId) {
        return recipeRepo.findById(recipeId).orElse(null);
    }

    @Transactional
    public String deleteRecipeById(long recipeId) {
        recipeRepo.deleteById(recipeId);
        return "Recipe deleted successfully";
    }

    @Transactional
    public Recipe updateRecipeById(long recipeId, RecipeRequestDto updatedRecipe) {

        int rows = recipeRepo.updateRecipe(
                updatedRecipe.getRecipeName(),
                updatedRecipe.getPrepTime(),
                updatedRecipe.getCategory(),
                updatedRecipe.getIngredients(),
                updatedRecipe.getRecipeInstructions(),
                updatedRecipe.getRecipeImageUrl(),
                updatedRecipe.getUserId(),
                recipeId
        );

        if (rows == 0) {
            throw new RuntimeException("Recipe not found or update failed");
        }
        return getRecipeById(recipeId);
    }

    private void triggerNotification(String recipeName, String email) {
        RecipeNotificationDto dto = new RecipeNotificationDto(
                email,
                "New Recipe Added",
                "Your recipe '" + recipeName + "' is now live!"
        );

        log.info("Publishing notification to RabbitMQ exchange: {} for email: {}",
                RabbitMQConfig.EXCHANGE, email);

        rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE, RabbitMQConfig.NOTIFICATION_ROUTING_KEY, dto);
    }
}