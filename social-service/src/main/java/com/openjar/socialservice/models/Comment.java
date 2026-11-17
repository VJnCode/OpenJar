package com.openjar.socialservice.models;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "comments", indexes = @Index(name = "idx_comment_recipe", columnList = "recipeId"))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Comment {

    @Id
    private String commentId;

    private Long recipeId;
    private String userId;

    /** Display name captured at posting time (from the JWT), so reads need no user lookup. */
    private String userName;

    @Column(columnDefinition = "TEXT")
    private String content;

    private LocalDateTime createdAt;

    private String parentId;

    @Builder.Default
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
