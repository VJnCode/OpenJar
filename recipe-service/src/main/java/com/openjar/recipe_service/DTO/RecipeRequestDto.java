package com.openjar.recipe_service.DTO;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

/** The owner is never part of the request body: it is taken from the authenticated JWT. */
@Data
public class RecipeRequestDto {

    @NotBlank(message = "Recipe name is required")
    @Size(min = 2, max = 100, message = "Recipe name must be between 2 and 100 characters")
    private String recipeName;

    @Min(value = 0, message = "Prep time cannot be negative")
    @Max(value = 1440, message = "Prep time must be at most 24 hours")
    private int prepTime;

    @NotBlank(message = "Category is required")
    @Size(max = 50)
    private String category;

    @NotBlank(message = "Add at least one ingredient")
    @Size(max = 10_000)
    private String ingredients;

    @NotBlank(message = "Add at least one instruction")
    @Size(max = 20_000)
    private String recipeInstructions;

    /** Only absolute http(s) URLs: blocks javascript:, data: and other dangerous schemes. */
    @Size(max = 1000)
    @Pattern(regexp = "^$|^https?://[^\\s\"'<>]+$", message = "Image URL must be an http(s) link")
    private String recipeImageUrl;
}
