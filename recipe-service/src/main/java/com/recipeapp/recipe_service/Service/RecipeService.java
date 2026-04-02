package com.recipeapp.recipe_service.Service;

import com.recipeapp.recipe_service.Configuration.RabbitMQConfig;
import com.recipeapp.recipe_service.DTO.RecipeNotificationDto;
import com.recipeapp.recipe_service.DTO.RecipeRequestDto;
import com.recipeapp.recipe_service.DTO.UserDto;
import com.recipeapp.recipe_service.Model.Recipe;
import com.recipeapp.recipe_service.Repository.RecipeRepo;
import lombok.extern.slf4j.Slf4j; // 1. Added Slf4j annotation
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.List;

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
            log.info("Recipe saved to DB. Fetching owner email from User-Service...");

            try {
                UserDto user = webClient.get()
                        .uri("/api/users/{id}", userId)
                        .retrieve()
                        .onStatus(HttpStatusCode::is4xxClientError, response -> {
                            log.error("User not found in User-Service for ID: {}", userId);
                            return Mono.error(new RuntimeException("User validation failed"));
                        })
                        .bodyToMono(UserDto.class)
                        .block();

                if (user != null && user.getUserEmail() != null) {
                    log.info("User Email retrieved: {}. Sending to RabbitMQ...", user.getUserEmail());

                    triggerNotification(recipe.getRecipeName(), "Added", user.getUserEmail());

                    return "Recipe added successfully for " + user.getUserEmail();
                } else {
                    log.error("User Service returned null for ID: {}", userId);
                    throw new RuntimeException("Could not retrieve user email");
                }

            } catch (Exception e) {
                log.error("Communication Error with User-Service: {}", e.getMessage());
                // In a production app, you might want to rollback the recipe save here
                // if the user doesn't actually exist.
                throw new RuntimeException("Failed to verify user: " + e.getMessage());
            }
        }

        return "Database error: Recipe could not be saved.";
    }

    private void triggerNotification(String recipeName, String action, String email) {
        RecipeNotificationDto notification = new RecipeNotificationDto(
                email,
                "Recipe " + action,
                "Success! Your recipe '" + recipeName + "' has been " + action.toLowerCase() + "."
        );

        log.debug("Sending message to Exchange: {}, RoutingKey: {}",
                RabbitMQConfig.EXCHANGE, RabbitMQConfig.NOTIFICATION_ROUTING_KEY);

        rabbitTemplate.convertAndSend(
                RabbitMQConfig.EXCHANGE,
                RabbitMQConfig.NOTIFICATION_ROUTING_KEY,
                notification
        );
        log.info("Message successfully queued for: {}", email);
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
}