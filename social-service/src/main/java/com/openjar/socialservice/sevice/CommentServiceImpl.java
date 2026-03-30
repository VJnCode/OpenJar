package com.openjar.socialservice.sevice;

import com.openjar.socialservice.Repository.CommentRepository;
import com.openjar.socialservice.dto.CommentRequestDto;
import com.openjar.socialservice.dto.CommentResponseDto;
import com.openjar.socialservice.models.Comment;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CommentServiceImpl implements CommentService {
    private final CommentRepository commentRepository;

    @Override
    public void postComment(CommentRequestDto requestDto) {
        String newId = java.util.UUID.randomUUID().toString();

        commentRepository.insertCommentNative(
                newId,
                requestDto.getRecipeId(),
                requestDto.getUserId(),
                requestDto.getContent()
        );
    }

    @Override
    public List<CommentResponseDto> getCommentsByRecipe(String recipeId) {
        return commentRepository.findByRecipeIdNative(recipeId).stream()
                .map(comment -> CommentResponseDto.builder()
                        .id(comment.getId()) // String to String - perfect!
                        .recipeId(comment.getRecipeId())
                        .userId(comment.getUserId())
                        .content(comment.getContent())
                        .createdAt(comment.getCreatedAt())
                        .build())
                .toList();
    }
}