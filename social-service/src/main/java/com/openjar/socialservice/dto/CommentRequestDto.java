package com.openjar.socialservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** The author is taken from the JWT; any client-supplied user id is ignored. */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CommentRequestDto {
    /** Required for top-level comments; replies take it from the URL. */
    @Pattern(regexp = "\\d{1,18}", message = "Invalid recipe id")
    private String recipeId;

    @NotBlank(message = "Comment cannot be empty")
    @Size(max = 2000, message = "Comments are limited to 2000 characters")
    private String content;
}
