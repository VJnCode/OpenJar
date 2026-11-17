package com.openjar.socialservice.models;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Table(name = "likes", uniqueConstraints = @UniqueConstraint(name = "uk_like_user_recipe", columnNames = {"user_id", "recipe_id"}))
@Data
public class LikeInteraction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long likeId;

    @Column(name = "recipe_id")
    private long recipeId;

    @Column(name = "user_id")
    private String userId;

    private LocalDateTime createdAt;
}
