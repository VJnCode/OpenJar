package com.openjar.recipe_service.DTO;

public record RecipeSyncMessage(
    Long recipeId,
    String recipeName,
    String category,
    String ingredients
) {}