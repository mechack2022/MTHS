package com.auth.service.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "password_resets")
@Setter
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PasswordReset {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // or AUTO
    private Long id;

    private String userId;

    private String resetToken;

    @Column(name = "expires_at")
    private LocalDateTime expiresAt;

    private boolean used;

    @Column(name = "created_at")
    private LocalDateTime createdAt;
}
