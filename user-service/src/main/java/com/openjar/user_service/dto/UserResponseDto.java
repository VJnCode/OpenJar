package com.openjar.user_service.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class UserResponseDto {
    private String userId;
    private String userName;
    private String userEmail;
    private LocalDateTime createdAt;
}