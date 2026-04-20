package com.openjar.socialservice.Events;


import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class LikeEvent {
    private Long recipeId;
    private int delta;
}
