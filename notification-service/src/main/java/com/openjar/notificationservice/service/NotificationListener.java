package com.openjar.notificationservice.service;

import com.openjar.notificationservice.dto.EmailNotificationDto;
import com.openjar.notificationservice.dto.NotificationRequest;
import com.openjar.notificationservice.dto.RecipeNotificationDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationListener {

    private final EmailService emailService;

    @RabbitListener(queues = "notification_queue")
    public void consumeRecipeNotification(RecipeNotificationDto recipeDto) {
        log.info("📬 Received Recipe Event from RabbitMQ!");
        log.info("Sending email to: {}", recipeDto.getUserEmail());

        try {
            // Map the Recipe DTO to your internal NotificationRequest DTO
            NotificationRequest request = new NotificationRequest(
                    recipeDto.getUserEmail(),
                    recipeDto.getSubject(),
                    recipeDto.getMessageBody()
            );

            emailService.sendAndLogEmail(request);
            log.info("✅ Recipe notification successfully delivered to {}", recipeDto.getUserEmail());

        } catch (Exception e) {
            log.error("❌ Failed to process recipe event: {}", e.getMessage());
        }
    }
}