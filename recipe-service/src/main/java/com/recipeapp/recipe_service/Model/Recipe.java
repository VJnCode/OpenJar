package com.recipeapp.recipe_service.Model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Entity
@Table(name="recipe")
public class Recipe {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long recipeId;
    @NotBlank(message = "Full name cannot be empty")
    @Size(min = 2, max = 50, message = "Recipe name must be between 3 and 50 characters")
    private String recipeName;
    private int prepTime;
    @NotBlank(message = " Category cannot be empty")
    private String category;
    @Lob
    private String ingredients;
    @Lob
    private String recipeInstructions;
    private String recipeImageUrl;
    @Column(name = "like_count")
    private long likeCount = 0;
    //    @ManyToOne
//    @JoinColumn(name="userId")
    private String userId;

}
