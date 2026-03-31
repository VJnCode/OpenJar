package com.recipeapp.recipe_service.Event;


import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class LikeEvent {
    private Long recipeId;
    private int delta;
}
