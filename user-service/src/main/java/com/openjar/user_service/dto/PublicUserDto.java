package com.openjar.user_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/** What anyone may see about a user. Deliberately excludes the email address. */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PublicUserDto {
    private String userId;
    private String userName;
    private LocalDateTime createdAt;
}
