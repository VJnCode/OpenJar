package com.openjar.socialservice.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CommentRequestDto {
    @NotBlank(message = "Recipe ID is required")
    private String recipeId;
    
    @NotBlank(message = "User ID is required")
    private String userId;
    
    @NotBlank(message = "Content cannot be empty")
    private String content;
}