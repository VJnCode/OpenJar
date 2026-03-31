package com.openjar.notificationservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RecipeNotificationDto {
    private String userEmail;
    private String subject;
    private String messageBody;
}