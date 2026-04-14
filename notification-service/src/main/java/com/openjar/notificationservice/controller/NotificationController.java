package com.openjar.notificationservice.controller;

import com.openjar.notificationservice.dto.EmailNotificationDto;
import com.openjar.notificationservice.dto.RecipeNotificationDto;
import com.openjar.notificationservice.service.EmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final EmailService emailService;

    @PostMapping("/test-template")
    public String sendTestTemplate(@RequestBody EmailNotificationDto request) { // Changed from RecipeNotificationDto
        emailService.sendHtmlNotification(request);
        return "HTML Template notification processed!";
    }
}