package com.openjar.user_service.models;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @Column(name = "user_id")
    private String userId;

    @Column(name = "user_name")
    private String userName;

    @Column(name = "user_email", unique = true)
    private String userEmail;

    @Column(name = "password")
    private String password;

    @Column(name = "is_verified")
    private Boolean isVerified;

    /** BCrypt hash of the one-time verification code (never stored in plain text). */
    @Column(name = "otp")
    private String otp;

    @Column(name = "otp_expires_at")
    private LocalDateTime otpExpiresAt;

    @Column(name = "otp_attempts")
    private Integer otpAttempts;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    public boolean isVerifiedAccount() {
        return Boolean.TRUE.equals(isVerified);
    }
}
