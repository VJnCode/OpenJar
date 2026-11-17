package com.openjar.socialservice.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
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
    private String userName;
    private String content;
    private LocalDateTime createdAt;
    @JsonProperty("deleted")
    private boolean isDeleted;
    private String parentId;
    private List<CommentResponseDto> replies;
}
