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
public interface CommentRepository extends JpaRepository<Comment, String> {

    @Modifying
    @Transactional
    @Query(value = "INSERT INTO comments (comment_id, recipe_id, user_id, content, parent_id, created_at) " +
            "VALUES (?1, ?2, ?3, ?4, ?5, NOW())", nativeQuery = true)
        // 1. String id, 2. Long recipeId, 3. String userId, 4. String content, 5. String parentId
    int insertCommentNative(String id, Long recipeId, String userId, String content, String parentId);

    @Query(value = "SELECT * FROM comments WHERE recipe_id = ?1", nativeQuery = true)
    List<Comment> findByRecipeIdNative(Long recipeId);
}