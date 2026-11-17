package com.openjar.user_service.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UserRequestDto {

    @NotBlank(message = "Username cannot be empty")
    @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
    @Pattern(regexp = "^[\\p{L}\\p{N} ._-]+$", message = "Username contains invalid characters")
    private String userName;

    @NotBlank(message = "Email cannot be empty")
    @Email(message = "Please provide a valid email address")
    @Size(max = 254)
    private String userEmail;

    // BCrypt ignores everything after 72 bytes, so cap it explicitly.
    @NotBlank(message = "Password cannot be empty")
    @Size(min = 8, max = 72, message = "Password must be between 8 and 72 characters")
    private String password;
}
