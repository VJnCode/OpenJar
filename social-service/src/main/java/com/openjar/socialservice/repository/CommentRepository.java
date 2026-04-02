package com.openjar.socialservice.repository;

import com.openjar.socialservice.models.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {

    @Query(value = "SELECT * FROM comments WHERE recipe_id = :recipeId ORDER BY created_at DESC",
            nativeQuery = true)
    List<Comment> findByRecipeIdNative(@Param("recipeId") String recipeId);

    @Modifying
    @Transactional
    @Query(value = "INSERT INTO comments (comment_id, recipe_id, user_id, content, created_at) " +
            "VALUES (:commentId, :recipeId, :userId, :content, NOW())",
            nativeQuery = true)
    void insertCommentNative(@Param("commentId") String commentId,
                             @Param("recipeId") String recipeId,
                             @Param("userId") String userId,
                             @Param("content") String content);
}