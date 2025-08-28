package com.auth.service.service;


import com.auth.service.constants.CodeType;
import com.auth.service.constants.VerificationResult;
import com.auth.service.entity.VerificationCode;

import java.util.Optional;

public interface VerificationService {

    // ========================================================================
    // CODE GENERATION AND STORAGE
    // ========================================================================

    /**
     * Save a verification code for a user
     * @param userId The user ID
     * @param code The verification code (will be hashed before storage)
     * @param type The type of verification code
     */
    void saveVerificationCode(String userId, String code, CodeType type);

    /**
     * Save a verification code with default EMAIL_VERIFICATION type
     * @deprecated Use saveVerificationCode(userId, code, type) instead
     */
    @Deprecated
    void saveVerificationCode(String userId, String code);

    // ========================================================================
    // CODE VALIDATION
    // ========================================================================

    /**
     * Validate a verification code and return detailed result
     * @param userId The user ID
     * @param inputCode The code to validate
     * @param type The type of verification code
     * @return VerificationResult indicating the outcome
     */
    VerificationResult validateVerificationCode(String userId, String inputCode, CodeType type);

    /**
     * Validate a verification code and throw exception if invalid
     * @param userId The user ID
     * @param inputCode The code to validate
     * @param type The type of verification code
     * @return VerificationResult.SUCCESS if valid
     * @throws BadRequestException if code is invalid, expired, or not found
     */
    VerificationResult validateAndThrowIfInvalid(String userId, String inputCode, CodeType type);

    /**
     * Simple boolean validation (deprecated - use validateVerificationCode for detailed results)
     * @deprecated Use validateVerificationCode() for better error handling
     */
    @Deprecated
    boolean isValidVerificationCode(String userId, String inputCode, CodeType type);

    // ========================================================================
    // CODE MANAGEMENT
    // ========================================================================

    /**
     * Check if user has a valid (non-expired, unused) verification code
     * @param userId The user ID
     * @param type The type of verification code
     * @return true if valid code exists
     */
    boolean hasValidCode(String userId, CodeType type);

    /**
     * Get active verification code for a user
     * @param userId The user ID
     * @param type The type of verification code
     * @return Optional containing the active code if exists
     */
    Optional<VerificationCode> getActiveVerificationCode(String userId, CodeType type);

    /**
     * Invalidate all existing codes for a user and type
     * @param userId The user ID
     * @param type The type of verification code
     */
    void invalidateExistingCodes(String userId, CodeType type);

    /**
     * Invalidate a specific verification code
     * @param userId The user ID
     * @param code The specific code to invalidate
     * @param type The type of verification code
     */
    void invalidateCode(String userId, String code, CodeType type);

    // ========================================================================
    // CODE RESEND
    // ========================================================================

    /**
     * Resend verification code (invalidates old and creates new)
     * @param userId The user ID
     * @param newCode The new verification code (will be hashed before storage)
     * @param type The type of verification code
     */
    void resendVerificationCode(String userId, String newCode, CodeType type);

    // ========================================================================
    // RATE LIMITING AND SECURITY
    // ========================================================================

    /**
     * Check if user can request a new code (rate limiting)
     * @param userId The user ID
     * @param type The type of verification code
     * @return true if user can request new code
     */
    boolean canRequestNewCode(String userId, CodeType type);

    /**
     * Get remaining time until user can request new code
     * @param userId The user ID
     * @param type The type of verification code
     * @return remaining seconds, 0 if can request immediately
     */
    long getRemainingCooldownSeconds(String userId, CodeType type);
}
