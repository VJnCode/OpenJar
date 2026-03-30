package com.openjar.notificationservice.controller; // Ensure this matches your folder!

import com.openjar.notificationservice.dto.NotificationRequest;
import com.openjar.notificationservice.service.EmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final EmailService emailService;

    @PostMapping("/test-email")
    public String sendTestEmail(@RequestBody NotificationRequest request) {
        emailService.sendAndLogEmail(request);
        return "Notification request sent to service layer!";
    }
}