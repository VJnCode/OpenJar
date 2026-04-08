package com.openjar.socialservice.Controller;

import com.openjar.socialservice.dto.CommentRequestDto;
import com.openjar.socialservice.dto.CommentResponseDto;
import com.openjar.socialservice.sevice.CommentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/social/comments")
@RequiredArgsConstructor
public class CommentController {
    private final CommentService commentService;

    @PostMapping("/add")
    public ResponseEntity<String> add(@Valid @RequestBody CommentRequestDto dto) {
        commentService.postComment(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body("Comment Added");
    }

    @GetMapping("/recipe/{recipeId}")
    public ResponseEntity<List<CommentResponseDto>> getCommentsByRecipe(@PathVariable String recipeId) {

        return ResponseEntity.ok(commentService.getCommentsByRecipe(recipeId));
    }

    @PostMapping("/{recipeId}/{parentId}")
    public ResponseEntity<String> saveReply(
            @PathVariable String recipeId,
            @PathVariable String parentId,
            @RequestBody CommentRequestDto dto,
            @RequestHeader("X-User-Id") String userId) {

        commentService.postReply(dto, recipeId, parentId, userId);

        return ResponseEntity.status(HttpStatus.CREATED).body("Reply posted successfully!");
    }
}