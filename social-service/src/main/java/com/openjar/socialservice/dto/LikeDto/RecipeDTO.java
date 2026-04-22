package com.openjar.socialservice.dto.LikeDto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RecipeDTO {
    private long recipeId;
    private String recipeName;
    private String userId;
}
