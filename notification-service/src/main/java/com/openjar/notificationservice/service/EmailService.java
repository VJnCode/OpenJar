package com.openjar.notificationservice.service;


import com.openjar.notificationservice.dto.NotificationRequest;
import com.openjar.notificationservice.models.NotificationLog;
import com.openjar.notificationservice.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;
    private final NotificationRepository repository;

    public void sendAndLogEmail(NotificationRequest request) {
        NotificationLog logEntry = NotificationLog.builder()
                .recipient(request.getRecipientEmail())
                .subject(request.getSubject())
                .content(request.getMessageBody())
                .sentAt(LocalDateTime.now())
                .build();

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(request.getRecipientEmail());
            message.setSubject(request.getSubject());
            message.setText(request.getMessageBody());
            
            mailSender.send(message);
            
            logEntry.setStatus("SENT");
            log.info("Successfully sent email to {}", request.getRecipientEmail());
        } catch (Exception e) {
            logEntry.setStatus("FAILED");
            log.error("Failed to send email to {}: {}", request.getRecipientEmail(), e.getMessage());
        } finally {
            repository.save(logEntry);
        }
    }
}