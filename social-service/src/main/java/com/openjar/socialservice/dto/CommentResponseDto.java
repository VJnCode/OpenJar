package com.openjar.socialservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CommentResponseDto {
    private String id;
    private String recipeId;
    private String userId;
    private String content;
    private LocalDateTime createdAt;
    private String parentId;
    private List<CommentResponseDto> replies;
}