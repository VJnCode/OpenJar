package com.openjar.socialservice.sevice;


import com.openjar.socialservice.dto.CommentRequestDto;
import com.openjar.socialservice.dto.CommentResponseDto;

import java.util.List;

public interface CommentService {
    void postComment(CommentRequestDto requestDto);
    List<CommentResponseDto> getCommentsByRecipe(String recipeId);

    void postReply(CommentRequestDto dto, String recipeId, String parentId, String userId);
}