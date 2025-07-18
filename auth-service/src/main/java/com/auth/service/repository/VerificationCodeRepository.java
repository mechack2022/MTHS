package com.auth.service.repository;

import com.auth.service.constants.CodeType;
import com.auth.service.entity.VerificationCode;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;


public interface VerificationCodeRepository extends JpaRepository<VerificationCode, Long> {

    @Modifying
    @Transactional
    @Query("UPDATE VerificationCode vc SET vc.used = true, vc.updatedAt = CURRENT_TIMESTAMP " +
            "WHERE vc.userId = :userId AND vc.used = false AND vc.expiresAt > CURRENT_TIMESTAMP")
    void markAllUnusedAsExpiredForUserId(@Param("userId") Long userId);

    Optional<VerificationCode> findByUserIdAndTypeAndUsedFalse(String userId, CodeType type);

    List<VerificationCode> findAllByUserIdAndTypeAndUsedFalse(String userId, CodeType type);

    @Query("SELECT vc FROM VerificationCode vc WHERE vc.userId = :userId " +
            "AND vc.type = :type AND vc.used = false AND vc.expiresAt > :now")
    Optional<VerificationCode> findActiveAndNonExpiredCode(
            @Param("userId") String userId,
            @Param("type") CodeType type,
            @Param("now") LocalDateTime now
    );
    List<VerificationCode> findAllByUserId(String userId);

    @Query("DELETE FROM VerificationCode vc WHERE vc.expiresAt < :expiryTime")
    void deleteExpiredCodes(@Param("expiryTime") LocalDateTime expiryTime);

    long countByUserIdAndTypeAndUsedFalse(String userId, CodeType type);
}

