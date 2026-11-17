package com.openjar.user_service.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

/** Only the display name can be changed; changing email here would bypass verification. */
@Data
public class UserUpdateDto {
    @NotBlank(message = "Username cannot be empty")
    @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
    @Pattern(regexp = "^[\\p{L}\\p{N} ._-]+$", message = "Username contains invalid characters")
    private String userName;
}
