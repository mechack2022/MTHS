package com.mths.auth.entity;

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
public class PasswordReset extends com.mths.shared.entity.BaseEntity {

    @Column(name="uuid", nullable = false, unique = true)
    private String uuid;

    private String userUuid;

    private String resetToken;

    @Column(name = "expires_at")
    private LocalDateTime expiresAt;

    private boolean used;

}
