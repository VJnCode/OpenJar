package com.openjar.socialservice.sevice;

import com.openjar.socialservice.config.CurrentUser;
import com.openjar.socialservice.dto.CommentResponseDto;

import java.util.List;

public interface CommentService {
    CommentResponseDto postComment(Long recipeId, String content, CurrentUser author);

    CommentResponseDto postReply(Long recipeId, String parentId, String content, CurrentUser author);

    List<CommentResponseDto> getCommentsByRecipe(Long recipeId);

    void deleteComment(String commentId, CurrentUser requester);
}
