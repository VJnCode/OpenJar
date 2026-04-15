package com.openjar.socialservice.Controller;

import com.openjar.socialservice.dto.ApiResponseDto;
import com.openjar.socialservice.dto.CommentRequestDto;
import com.openjar.socialservice.dto.CommentResponseDto;
import com.openjar.socialservice.sevice.CommentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/social/comments")
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;

    @PostMapping("/add")
    public ResponseEntity<ApiResponseDto<CommentResponseDto>> add(@Valid @RequestBody CommentRequestDto dto) {
        CommentResponseDto savedComment = commentService.postComment(dto);

        ApiResponseDto<CommentResponseDto> response = ApiResponseDto.<CommentResponseDto>builder()
                .message("Comment Added Successfully")
                .success(true)
                .data(savedComment)
                .build();

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/recipe/{recipeId}")
    public ResponseEntity<ApiResponseDto<List<CommentResponseDto>>> getCommentsByRecipe(@PathVariable String recipeId) {
        List<CommentResponseDto> comments = commentService.getCommentsByRecipe(recipeId);

        ApiResponseDto<List<CommentResponseDto>> response = ApiResponseDto.<List<CommentResponseDto>>builder()
                .message("Comments fetched successfully")
                .success(true)
                .data(comments)
                .build();

        return ResponseEntity.ok(response);
    }

    @PostMapping("/{recipeId}/{parentId}")
    public ResponseEntity<ApiResponseDto<CommentResponseDto>> saveReply(
            @PathVariable String recipeId,
            @PathVariable String parentId,
            @RequestBody CommentRequestDto dto,
            @RequestHeader("X-User-Id") String userId) {

        CommentResponseDto savedReply = commentService.postReply(dto, recipeId, parentId, userId);

        ApiResponseDto<CommentResponseDto> response = ApiResponseDto.<CommentResponseDto>builder()
                .message("Reply posted successfully!")
                .success(true)
                .data(savedReply)
                .build();

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}