package com.openjar.user_service.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ResendOtpRequestDto {
    @NotBlank
    @Email
    private String email;
}
