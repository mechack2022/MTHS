package com.auth.service.repository;

import com.auth.service.entity.PasswordReset;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.Optional;
public interface PasswordResetRepository extends JpaRepository<PasswordReset, Long> {
    Optional<PasswordReset> findByUserUuidAndUsedFalseAndExpiresAtAfter(String userId, LocalDateTime now);
}