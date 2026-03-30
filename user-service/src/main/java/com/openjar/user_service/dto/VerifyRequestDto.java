package com.openjar.user_service.dto;
import lombok.Data;

@Data
public class VerifyRequestDto {
    private String email;
    private String otp;
}