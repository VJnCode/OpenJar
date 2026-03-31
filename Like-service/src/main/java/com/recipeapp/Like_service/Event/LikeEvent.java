package com.recipeapp.Like_service.Event;


import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class LikeEvent {
    private Long recipeId;
    private int delta;
}
