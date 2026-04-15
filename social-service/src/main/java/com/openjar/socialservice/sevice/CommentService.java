package com.openjar.socialservice.sevice;

import com.openjar.socialservice.dto.CommentRequestDto;
import com.openjar.socialservice.dto.CommentResponseDto;
import java.util.List;

public interface CommentService {
    CommentResponseDto postComment(CommentRequestDto requestDto);
    CommentResponseDto postReply(CommentRequestDto dto, String recipeIdStr, String parentId, String userId);
    List<CommentResponseDto> getCommentsByRecipe(String recipeIdStr);
}