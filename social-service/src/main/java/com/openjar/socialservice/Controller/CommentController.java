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
        // The service now returns DTOs, so this matches perfectly
        return ResponseEntity.ok(commentService.getCommentsByRecipe(recipeId));
    }
}