package com.openjar.socialservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CommentRequestDto {

    @NotBlank(message = "Recipe ID is required")
    private String recipeId;

    @NotBlank(message = "User ID is required")
    private String userId;

    @NotBlank(message = "Content cannot be empty")
    @Size(max = 1000, message = "Comment must be less than 1000 characters")
    private String content;
}