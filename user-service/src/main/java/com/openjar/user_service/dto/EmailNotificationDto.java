package com.openjar.user_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class EmailNotificationDto {
    private String userEmail;
    private String messageBody;
    private String subject;
}