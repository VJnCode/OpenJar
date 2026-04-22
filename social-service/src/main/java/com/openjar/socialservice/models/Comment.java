package com.openjar.socialservice.models;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "comments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Comment {

    @Id
    private String commentId;

    private String recipeId;
    private String userId;

    @Column(columnDefinition = "TEXT")
    private String content;

    private LocalDateTime createdAt;

    private String parentId;

    @Column(name = "is_deleted")
    private boolean isDeleted = false;

    @PrePersist
    protected void onCreate() {
        if (this.commentId == null) {
            this.commentId = java.util.UUID.randomUUID().toString();
        }
        this.createdAt = LocalDateTime.now();
    }


}