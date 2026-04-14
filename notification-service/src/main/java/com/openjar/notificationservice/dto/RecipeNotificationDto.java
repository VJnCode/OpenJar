package com.openjar.notificationservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor

public class RecipeNotificationDto {
    private String recipientEmail;
    private String subject;
    private String templateName;
    private Map<String, Object> templateModel;
}