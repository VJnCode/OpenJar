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


    public String saveRecipe( RecipeRequestDto recipe, String userId){
//        try {
//            userClient.getUserById(recipe.getUserId());
//        } catch (Exception e) {
//            throw new RuntimeException("User does not exist");
//        }

        int rows = recipeRepo.insertRecipe(
                recipe.getRecipeName(),
                recipe.getPrepTime(),
                recipe.getCategory(),
                recipe.getIngredients(),
                recipe.getRecipeInstructions(),
                recipe.getRecipeImageUrl(),
               userId
        );

        System.out.println(">>> DB Insert Rows: " + rows);

        if (rows > 0) {
            try {//changed the way we get userId from dto to pathVariable
                System.out.println(">>> Calling User Service for ID: " + userId);

                UserDto user = webClient.get()
                        .uri("/api/users/{id}", userId)
                        .retrieve()
                        .bodyToMono(UserDto.class)
                        .block();

                if (user != null && user.getUserEmail() != null) {
                    System.out.println(">>> User found! Email: " + user.getUserEmail());
                    triggerNotification(recipe.getRecipeName(), "Added", user.getUserEmail());
                } else {
                    System.out.println(">>> WARNING: User Service returned null or empty email.");
                    triggerNotification(recipe.getRecipeName(), "Added (Manual)", "varun@example.com");
                }
            } catch (Exception e) {
                System.err.println(">>> Notification System Error: " + e.getMessage());

                triggerNotification(recipe.getRecipeName(), "Added (Error Fallback)", "varun@example.com");
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

// added the deleteRecipe option
    @Transactional
    public String deleteRecipeById(long recipeId){

        Recipe recipe = recipeRepo.getRecipeById(recipeId);

        if(recipe == null){
            throw new RuntimeException("Recipe not found");
        }

        int rows = recipeRepo.deleteRecipeById(recipeId);

        if(rows > 0){
            return "Recipe deleted successfully";
        } else {
            return "Recipe was not deleted";
        }
    }

    @Transactional
    public Recipe updateRecipeById(long recipeId ,  RecipeRequestDto updatedRecipe , String userId){



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

        if(rows == 0){
            throw new RuntimeException("Recipe not found");
        }

        return recipeRepo.getRecipeById(recipeId);
    }













}
