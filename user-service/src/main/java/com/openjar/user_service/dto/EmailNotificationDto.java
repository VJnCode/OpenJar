package com.openjar.user_service.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class EmailNotificationDto {
    private String recipientEmail;
    private String subject;
    private String templateName;
    private Map<String, Object> templateModel;
}