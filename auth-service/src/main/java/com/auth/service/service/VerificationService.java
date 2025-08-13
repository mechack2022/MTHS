package com.auth.service.service;


import com.auth.service.constants.CodeType;
import com.auth.service.constants.VerificationResult;
import com.auth.service.entity.VerificationCode;

import java.util.Optional;

public interface VerificationService {

    void saveVerificationCode(String userId, String code);

    boolean isValidVerificationCode(String userId, String inputCode, CodeType type);

    /**
     * Validate a verification code and return detailed result
     * @param userId The user ID
     * @param inputCode The code to validate
     * @param type The type of verification code
     * @return VerificationResult indicating the outcome
     */
    VerificationResult validateVerificationCode(String userId, String inputCode, CodeType type);

    VerificationResult validateAndThrowIfInvalid(String userId, String inputCode, CodeType type);


    /**
     * Check if user has a valid (non-expired, unused) verification code
     * @param userId The user ID
     * @param type The type of verification code
     * @return true if valid code exists
     */
    boolean hasValidCode(String userId, CodeType type);

    /**
     * Invalidate all existing codes for a user and type
     * @param userId The user ID
     * @param type The type of verification code
     */
    void invalidateExistingCodes(String userId, CodeType type);

    /**
     * Get active verification code for a user
     * @param userId The user ID
     * @param type The type of verification code
     * @return Optional containing the active code if exists
     */
    Optional<VerificationCode> getActiveVerificationCode(String userId, CodeType type);

    /**
     * Resend verification code (invalidates old and creates new)
     * @param userId The user ID
     * @param newCode The new verification code
     * @param type The type of verification code
     */
    void resendVerificationCode(String userId, String newCode, CodeType type);
    void saveVerificationCode(String userId, String code, CodeType type);
    void invalidateCode(String userId, String code, CodeType type);
}
