package com.openjar.socialservice.Controller;

import com.openjar.socialservice.config.CurrentUser;
import com.openjar.socialservice.dto.ApiResponseDto;
import com.openjar.socialservice.dto.CommentRequestDto;
import com.openjar.socialservice.dto.CommentResponseDto;
import com.openjar.socialservice.sevice.CommentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/social/comments")
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;

    @PostMapping("/add")
    public ResponseEntity<ApiResponseDto<CommentResponseDto>> add(@Valid @RequestBody CommentRequestDto dto,
                                                                  @AuthenticationPrincipal Jwt jwt) {
        if (dto.getRecipeId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Recipe id is required");
        }
        CommentResponseDto saved = commentService.postComment(
                Long.parseLong(dto.getRecipeId()), dto.getContent(), CurrentUser.from(jwt));
        return ResponseEntity.status(HttpStatus.CREATED).body(ok("Comment added", saved));
    }

    @GetMapping("/recipe/{recipeId}")
    public ApiResponseDto<List<CommentResponseDto>> getCommentsByRecipe(@PathVariable Long recipeId) {
        return ok("Comments fetched", commentService.getCommentsByRecipe(recipeId));
    }

    @PostMapping("/{recipeId}/{parentId}")
    public ResponseEntity<ApiResponseDto<CommentResponseDto>> reply(@PathVariable Long recipeId,
                                                                    @PathVariable String parentId,
                                                                    @Valid @RequestBody CommentRequestDto dto,
                                                                    @AuthenticationPrincipal Jwt jwt) {
        CommentResponseDto saved = commentService.postReply(recipeId, parentId, dto.getContent(), CurrentUser.from(jwt));
        return ResponseEntity.status(HttpStatus.CREATED).body(ok("Reply posted", saved));
    }

    @DeleteMapping("/{commentId}")
    public ApiResponseDto<Void> delete(@PathVariable String commentId, @AuthenticationPrincipal Jwt jwt) {
        commentService.deleteComment(commentId, CurrentUser.from(jwt));
        return ok("Comment deleted", null);
    }

    private static <T> ApiResponseDto<T> ok(String message, T data) {
        return ApiResponseDto.<T>builder().message(message).success(true).data(data).build();
    }
}
