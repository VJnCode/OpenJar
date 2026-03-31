package com.recipeapp.recipe_service.DTO;

import lombok.Data;

@Data
public class UserDto {
    private long userId;
    private String fullName;
    private String email;
}
