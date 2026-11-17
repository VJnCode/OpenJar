package com.openjar.recipe_service.Model;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "recipe", indexes = @Index(name = "idx_recipe_user", columnList = "userId"))
public class Recipe {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long recipeId;

    private String recipeName;

    private int prepTime;

    private String category;

    @Lob
    private String ingredients;

    @Lob
    private String recipeInstructions;

    @Column(length = 1000)
    private String recipeImageUrl;

    @Column(name = "like_count")
    private long likeCount = 0;

    private String userId;
}
