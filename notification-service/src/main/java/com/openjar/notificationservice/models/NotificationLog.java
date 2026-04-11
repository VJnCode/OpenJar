package com.openjar.notificationservice.models;

import jakarta.persistence.*;
import lombok.*;
//import jakarta.ws.rs.client.Entity;
//import lombok.*;
//import org.hibernate.annotations.Table;
//import org.hibernate.mapping.Column;
//import org.springframework.data.annotation.Id;

import java.time.LocalDateTime;

@Entity
@Table(name = "notification_logs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String recipient;
    private String subject;
    
    @Column(columnDefinition = "TEXT")
    private String content;
    
    private String status;
    private LocalDateTime sentAt;
}