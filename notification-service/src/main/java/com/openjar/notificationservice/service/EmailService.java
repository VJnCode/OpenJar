package com.openjar.notificationservice.service;

import com.openjar.notificationservice.dto.EmailNotificationDto; // Updated DTO name
import com.openjar.notificationservice.models.NotificationLog;
import com.openjar.notificationservice.repository.NotificationRepository;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;
    private final NotificationRepository repository;
    private final TemplateEngine templateEngine;

    /**
     * Processes and sends an HTML email based on a Thymeleaf template.
     * * @param request The DTO containing recipient, subject, template name, and data map.
     */
    /** Only these templates may be rendered; anything else in a message is rejected. */
    private static final java.util.Set<String> ALLOWED_TEMPLATES = java.util.Set.of(
            "otp-template", "recipe-created", "comment-notification", "like-created", "reciepe-alert");

    public void sendHtmlNotification(EmailNotificationDto request) {
        if (request == null || request.getRecipientEmail() == null
                || !ALLOWED_TEMPLATES.contains(request.getTemplateName())) {
            log.warn("Rejected notification with unknown template '{}'",
                    request == null ? null : request.getTemplateName());
            return;
        }
        NotificationLog logEntry = NotificationLog.builder()
                .recipient(request.getRecipientEmail())
                .subject(request.getSubject())
                .sentAt(LocalDateTime.now())
                .build();

        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");

            Context context = new Context();
            if (request.getTemplateModel() != null) {
                context.setVariables(request.getTemplateModel());
            }

            String htmlContent = templateEngine.process(request.getTemplateName(), context);

            helper.setTo(request.getRecipientEmail());
            helper.setSubject(request.getSubject());
            helper.setText(htmlContent, true);
            mailSender.send(mimeMessage);

            logEntry.setContent("Template: " + request.getTemplateName() + " | Subject: " + request.getSubject());
            logEntry.setStatus("SENT");
            log.info("✅ HTML Email successfully delivered to: {}", request.getRecipientEmail());

        } catch (Exception e) {
            logEntry.setStatus("FAILED");
            logEntry.setContent("Error: " + e.getMessage());
            log.error("❌ Failed to process/send template email to {}: {}", request.getRecipientEmail(), e.getMessage());
        } finally {
            repository.save(logEntry);
        }
    }
}