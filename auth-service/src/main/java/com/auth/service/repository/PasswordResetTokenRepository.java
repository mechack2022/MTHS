package com.auth.service.repository;

import com.auth.service.entity.PasswordResetToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {

    Optional<PasswordResetToken> findByTokenAndUsedFalse(String token);

    Optional<PasswordResetToken> findByUserUuidAndUsedFalse(String userUuid);

    @Modifying
    @Query("UPDATE PasswordResetToken prt SET prt.used = true, prt.updatedAt = CURRENT_TIMESTAMP " +
            "WHERE prt.userUuid = :userUuid AND prt.used = false")
    void invalidateAllTokensForUser(@Param("userUuid") String userUuid);

    @Modifying
    @Query("DELETE FROM PasswordResetToken prt WHERE prt.expiresAt < :now")
    void deleteExpiredTokens(@Param("now") LocalDateTime now);

    boolean existsByUserUuidAndUsedFalse(String userUuid);
}