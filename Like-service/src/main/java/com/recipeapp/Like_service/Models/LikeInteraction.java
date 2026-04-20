package com.recipeapp.Like_service.Models;

import lombok.Data;
import jakarta.persistence.*;

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
