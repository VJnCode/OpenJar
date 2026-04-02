package com.openjar.socialservice.Repository;

import com.openjar.socialservice.models.Comment;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {

    // Add resultClass = Comment.class so it doesn't return "Object"
    @Query(value = "SELECT * FROM comments WHERE recipe_id = :recipeId ORDER BY created_at DESC",
            nativeQuery = true)
    List<Comment> findByRecipeIdNative(@Param("recipeId") String recipeId);

    @Modifying
    @Transactional
    @Query(value = "INSERT INTO comments (recipe_id, user_id, content, created_at) " +
            "VALUES (:recipeId, :userId, :content, NOW())",
            nativeQuery = true)
    void insertCommentNative(@Param("recipeId") String recipeId,
                             @Param("userId") String userId,
                             @Param("content") String content, @NotBlank(message = "Content cannot be empty") @Size(max = 1000, message = "Comment must be less than 1000 characters") String requestDtoContent);
}