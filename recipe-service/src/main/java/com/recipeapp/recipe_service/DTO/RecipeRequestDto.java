package com.recipeapp.recipe_service.DTO;


import lombok.Data;

@Data
public class RecipeRequestDto {

        private String recipeName;
        private int prepTime;
        private String category;
        private String ingredients;
        private String recipeInstructions;
        private String recipeImageUrl;
        private String userId;
}
