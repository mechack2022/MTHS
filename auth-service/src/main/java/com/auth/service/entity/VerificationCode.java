package com.auth.service.entity;

import com.auth.service.constants.CodeType;
import com.auth.service.constants.Medium;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "verification_codes")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VerificationCode extends BaseEntity{

    @Column(name="uuid", nullable = false, unique = true)
    private String uuid;

    private String userUuid;

    @Enumerated(EnumType.STRING)
    private Medium medium;

    private String target;
    // hash code
    private String code;

    @Enumerated(EnumType.STRING)
    private CodeType type;

    @Column(name = "expires_at")
    private LocalDateTime expiresAt;

    private boolean used;

//    @Column(name = "created_at")
//    private LocalDateTime createdAt;
//
//    @Column(name = "updated_at")
//    private LocalDateTime updatedAt;
}
