package com.openjar.socialservice.repository;

import com.openjar.socialservice.models.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommentRepository extends JpaRepository<Comment, String> {

    List<Comment> findByRecipeIdOrderByCreatedAtAsc(Long recipeId);
}
