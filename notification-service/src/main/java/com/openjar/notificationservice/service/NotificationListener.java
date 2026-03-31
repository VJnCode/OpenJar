package com.openjar.notificationservice.service;

import com.openjar.notificationservice.dto.EmailNotificationDto;
import com.openjar.notificationservice.dto.NotificationRequest;
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
    public void consumeNotificationEvent(EmailNotificationDto emailDto) {
        log.info("📬 Received Notification Event from RabbitMQ!");
        log.info("Sending email to: {}", emailDto.getEmail());

        try {
            String emailBody = "Hello!\n\n" +
                    "Welcome to OpenJar. Your verification code is: " + emailDto.getOtp() + "\n\n" +
                    "Please enter this code to verify your account.";

            NotificationRequest request = new NotificationRequest(
                    emailDto.getEmail(),
                    emailDto.getSubject(),
                    emailBody
            );

            emailService.sendAndLogEmail(request);

            log.info("✅ OTP email successfully delivered to {}", emailDto.getEmail());

        } catch (Exception e) {
            log.error("❌ Failed to process email event for {}: {}", emailDto.getEmail(), e.getMessage());
        }
    }
}