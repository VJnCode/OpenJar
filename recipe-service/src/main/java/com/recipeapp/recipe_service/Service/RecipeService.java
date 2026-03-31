package com.recipeapp.recipe_service.Service;


import com.recipeapp.recipe_service.DTO.RecipeRequestDto;
import com.recipeapp.recipe_service.DTO.UserDto;
//import com.recipeapp.recipe_service.Feign.UserClient;
import com.recipeapp.recipe_service.Model.Recipe;
import com.recipeapp.recipe_service.Repository.RecipeRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class RecipeService {


    private RecipeRepo recipeRepo;
//    @Autowired
//    private UserClient userClient;

    public RecipeService(RecipeRepo recipeRepo){
        this.recipeRepo = recipeRepo;
    }


    public String saveRecipe( RecipeRequestDto recipe,long userId){
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

        if(rows > 0){
            return "Recipe added successfully";
        } else {
            return "Recipe cannot be added";
        }
    }


    public List<Recipe> getAllRecipe(){
        return recipeRepo.getAllRecipe();
    }


    public Recipe getRecipeById(long recipeId){
        return recipeRepo.getRecipeById(recipeId);
    }


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
    public Recipe updateRecipeById(long recipeId ,  RecipeRequestDto updatedRecipe, long userId){

//        try {
//            userClient.getUserById( updatedRecipe.getUserId());
//        } catch (Exception e) {
//            throw new RuntimeException("User does not exist");
//        }

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
