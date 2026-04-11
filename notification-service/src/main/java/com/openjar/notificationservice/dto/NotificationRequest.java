package com.openjar.notificationservice.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class NotificationRequest {

    @Email(message = "Invalid recipient email")
    @NotBlank(message = "Recipient email is required")
    private String recipientEmail;

    @NotBlank(message = "Message body is required")
    private String messageBody;

    @NotBlank(message = "Subject is required")
    private String subject;


}