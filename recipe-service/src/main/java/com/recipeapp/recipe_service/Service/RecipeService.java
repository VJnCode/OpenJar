package com.recipeapp.recipe_service.Service;

import com.recipeapp.recipe_service.Configuration.RabbitMQConfig;
import com.recipeapp.recipe_service.DTO.RecipeNotificationDto;
import com.recipeapp.recipe_service.DTO.RecipeRequestDto;
import com.recipeapp.recipe_service.DTO.UserDto;
import com.recipeapp.recipe_service.Model.Recipe;
import com.recipeapp.recipe_service.Repository.RecipeRepo;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
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
    public String saveRecipe(RecipeRequestDto recipeRequest) {
        int rows = recipeRepo.insertRecipe(
                recipeRequest.getRecipeName(),
                recipeRequest.getPrepTime(),
                recipeRequest.getCategory(),
                recipeRequest.getIngredients(),
                recipeRequest.getRecipeInstructions(),
                recipeRequest.getRecipeImageUrl(),
                recipeRequest.getUserId()
        );

        System.out.println(">>> DB Insert Rows: " + rows);

        if (rows > 0) {
            try {
                System.out.println(">>> Calling User Service for ID: " + recipeRequest.getUserId());

                UserDto user = webClient.get()
                        .uri("/api/users/{id}", recipeRequest.getUserId())
                        .retrieve()
                        .bodyToMono(UserDto.class)
                        .block();

                if (user != null && user.getUserEmail() != null) {
                    System.out.println(">>> User found! Email: " + user.getUserEmail());
                    triggerNotification(recipeRequest.getRecipeName(), "Added", user.getUserEmail());
                } else {
                    System.out.println(">>> WARNING: User Service returned null or empty email.");
                    triggerNotification(recipeRequest.getRecipeName(), "Added (Manual)", "varun@example.com");
                }
            } catch (Exception e) {
                System.err.println(">>> Notification System Error: " + e.getMessage());

                triggerNotification(recipeRequest.getRecipeName(), "Added (Error Fallback)", "varun@example.com");
            }
            return "Recipe added successfully";
        }

        return "Recipe cannot be added";
    }

    private void triggerNotification(String recipeName, String action, String email) {

        RecipeNotificationDto notification = new RecipeNotificationDto(
                email,
                "Recipe " + action,
                "Success! Your recipe '" + recipeName + "' has been " + action.toLowerCase() + "."
        );

        rabbitTemplate.convertAndSend(
                RabbitMQConfig.EXCHANGE,
                RabbitMQConfig.NOTIFICATION_ROUTING_KEY,
                notification
        );
        System.out.println(">>> Notification sent to RabbitMQ for: " + email);
    }

    // Existing methods (getAll, getById, delete, update) remain the same
    public List<Recipe> getAllRecipe() { return recipeRepo.getAllRecipe(); }
    public Recipe getRecipeById(long id) { return recipeRepo.getRecipeById(id); }

    @Transactional
    public String deleteRecipeById(long id) {
        int rows = recipeRepo.deleteRecipeById(id);
        return rows > 0 ? "Deleted" : "Not Found";
    }
}