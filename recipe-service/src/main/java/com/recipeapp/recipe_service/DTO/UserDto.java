package com.recipeapp.recipe_service.DTO;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class UserDto {
    private String userId;
    private String fullName;
    private String userEmail;
}
