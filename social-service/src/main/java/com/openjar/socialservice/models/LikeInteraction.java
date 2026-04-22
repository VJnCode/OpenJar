package com.openjar.socialservice.models;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Table(name="likes")
@Data
public class LikeInteraction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long likeId;
    private long recipeId;
    private String userId;
    private LocalDateTime createdAt;

}
