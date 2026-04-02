package com.openjar.socialservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecipeResponseDto {
    private Long recipeId;
    private String recipeName;
    private String userId;
    private String category;
    private String recipeImageUrl;

}