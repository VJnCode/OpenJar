package com.openjar.recommendation.dto;

public record RecipeSyncMessage(
    Long recipeId,
    String recipeName,
    String category,
    String ingredients
) {}