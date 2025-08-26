package com.auth.service.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "password_reset_tokens")
@Setter
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PasswordResetToken extends BaseEntity {

    @Column(name = "token", nullable = false, unique = true, length = 255)
    private String token;

    @Column(name = "user_uuid", nullable = false)
    private String userUuid;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    @Column(name = "used", nullable = false)
    @Builder.Default
    private Boolean used = false;

    @Column(name = "verification_code_used", nullable = false, length = 100)
    private String verificationCodeUsed; // Store hash of the verification code that was used

    @PrePersist
    private void generateToken() {
        if (this.token == null) {
            this.token = UUID.randomUUID().toString().replace("-", "") + 
                        UUID.randomUUID().toString().replace("-", "");
        }
    }

    public boolean isValid() {
        return !used && expiresAt.isAfter(LocalDateTime.now());
    }

    public void markAsUsed() {
        this.used = true;
    }
}