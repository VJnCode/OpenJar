package com.openjar.notificationservice.service;

import com.openjar.notificationservice.dto.EmailNotificationDto;
import com.openjar.notificationservice.dto.NotificationRequest;
import com.openjar.notificationservice.dto.RecipeNotificationDto;
import com.recipeapp.Like_service.DTO.LikeNotificationDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationListener {

    private final EmailService emailService;

    @RabbitListener(queues = "notification_queue")
    public void consumeRecipeNotification(EmailNotificationDto emailDto) { // Changed from RecipeNotificationDto
        log.info("📬 Received Template-based Notification for {}", emailDto.getRecipientEmail());

        try {
            emailService.sendHtmlNotification(emailDto);
            log.info("✅ Delivered template: {} to {}", emailDto.getTemplateName(), emailDto.getRecipientEmail());
        } catch (Exception e) {
            log.error("❌ Notification Listener Error: {}", e.getMessage());
        }
    }





    @RabbitListener(queues ="recipe_like_queue")
    public void consumeLikeNotification(EmailNotificationDto emailDto) { // Changed from RecipeNotificationDto
        log.info("📬 Received Template-based Notification for {}", emailDto.getRecipientEmail());

        try {
            emailService.sendHtmlNotification(emailDto);
            log.info("✅ Delivered template: {} to {}", emailDto.getTemplateName(), emailDto.getRecipientEmail());
        } catch (Exception e) {
            log.error("❌ Notification Listener Error: {}", e.getMessage());
        }
    }




}