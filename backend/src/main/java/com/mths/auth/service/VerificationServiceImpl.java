package com.mths.auth.service;


import com.mths.shared.constants.CodeType;
import com.mths.shared.constants.Medium;
import com.mths.shared.constants.VerificationResult;
import com.mths.auth.entity.VerificationCode;
import com.mths.shared.exceptions.BadRequestException;
import com.mths.auth.repository.VerificationCodeRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
@AllArgsConstructor
@Slf4j
public class VerificationServiceImpl implements VerificationService {

    private final VerificationCodeRepository verificationCodeRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void saveVerificationCode(String userId, String code) {
        saveVerificationCode(userId, code, CodeType.EMAIL_VERIFICATION);
    }

    @Override
    @Transactional
    public void saveVerificationCode(String userUuid, String code, CodeType type) {
        // Invalidate any existing unused codes for this user and type
        invalidateExistingCodes(userUuid, type);

        VerificationCode verificationCode = VerificationCode.builder()
                .uuid(UUID.randomUUID().toString())
                .userUuid(userUuid)
                .medium(Medium.EMAIL)
                .target("email")
                .code(code)
                .type(type)
                .expiresAt(LocalDateTime.now().plusMinutes(getExpirationMinutes(type)))
                .used(false)

//                .createdAt(LocalDateTime.now())
//                .updatedAt(LocalDateTime.now())
                .build();

        verificationCodeRepository.save(verificationCode);
        log.info("Verification code saved for userId: {} with type: {} and expiry: {}",
                userUuid, type, verificationCode.getExpiresAt());
    }

    @Override
    @Transactional
    public VerificationResult validateVerificationCode(String userUuid, String inputCode, CodeType type) {
        log.info("Validating verification code for userUuid: {}, type: {}", userUuid, type);

        Optional<VerificationCode> verificationCodeOpt = verificationCodeRepository
                .findByUserUuidAndTypeAndUsedFalse(userUuid, type);

        if (verificationCodeOpt.isEmpty()) {
            log.warn("No active verification code found for userUuid: {}, type: {}", userUuid, type);
            return VerificationResult.CODE_NOT_FOUND;
        }

        VerificationCode verificationCode = verificationCodeOpt.get();

        // Check if code has expired
        if (verificationCode.getExpiresAt().isBefore(LocalDateTime.now())) {
            log.warn("Verification code expired for userUuid: {}. Expired at: {}", userUuid, verificationCode.getExpiresAt());
            return VerificationResult.CODE_EXPIRED;
        }

        // Check if code matches
        if ( !passwordEncoder.matches(inputCode, verificationCode.getCode())) {
            log.warn("Invalid verification code provided for userUuid: {}", userUuid);
            return VerificationResult.INVALID_CODE;
        }

        // Mark code as used
        verificationCode.setUsed(true);
        verificationCode.setUpdatedAt(LocalDateTime.now());
        verificationCodeRepository.save(verificationCode);

        log.info("Verification code successfully validated for userUuid: {}", userUuid);
        return VerificationResult.SUCCESS;
    }

    @Override
    @Deprecated
    public boolean isValidVerificationCode(String userUuid, String inputCode, CodeType type) {
        // Keep for backward compatibility
        return validateVerificationCode(userUuid, inputCode, type) == VerificationResult.SUCCESS;
    }

    @Override
    public VerificationResult validateAndThrowIfInvalid(String userUuid, String inputCode, CodeType type) {
        VerificationResult result = validateVerificationCode(userUuid, inputCode, type);

        switch (result) {
            case SUCCESS:
                return result; // Valid code, proceed

            case CODE_NOT_FOUND:
                throw new BadRequestException(
                        "No active verification code found for this user. Please request a new code.",
                        "VERIFICATION_CODE_NOT_FOUND"
                );

            case CODE_EXPIRED:
                throw new BadRequestException(
                        "The verification code has expired. Please request a new code.",
                        "VERIFICATION_CODE_EXPIRED"
                );

            case INVALID_CODE:
                throw new BadRequestException(
                        "The verification code provided is incorrect. Please check and try again.",
                        "INVALID_VERIFICATION_CODE"
                );

            case CODE_ALREADY_USED:
                throw new BadRequestException(
                        "This verification code has already been used. Please request a new code.",
                        "VERIFICATION_CODE_ALREADY_USED"
                );

            default:
                throw new BadRequestException(
                        "Unable to verify the code. Please try again or request a new code.",
                        "VERIFICATION_FAILED"
                );
        }
    }

    @Override
    public boolean hasValidCode(String userUuid, CodeType type) {
        return verificationCodeRepository.findByUserUuidAndTypeAndUsedFalse(userUuid, type)
                .filter(vc -> vc.getExpiresAt().isAfter(LocalDateTime.now()))
                .isPresent();
    }

    @Override
    @Transactional
    public void invalidateExistingCodes(String userUuid, CodeType type) {
        verificationCodeRepository.findAllByUserUuidAndTypeAndUsedFalse(userUuid, type)
                .forEach(code -> {
                    code.setUsed(true);
                    code.setUpdatedAt(LocalDateTime.now());
                    verificationCodeRepository.save(code);
                });
        log.info("Invalidated existing verification codes for userUuid: {}, type: {}", userUuid, type);
    }

    @Override
    @Transactional
    public void invalidateCode(String userUuid, String code, CodeType type) {
        // This method is for invalidating a specific code (used after password reset success)
        verificationCodeRepository.findByUserUuidAndTypeAndUsedFalse(userUuid, type)
                .filter(vc -> vc.getCode().equals(code))
                .ifPresent(verificationCode -> {
                    verificationCode.setUsed(true);
                    verificationCode.setUpdatedAt(LocalDateTime.now());
                    verificationCodeRepository.save(verificationCode);
                    log.info("Invalidated specific verification code for userUuid: {}, type: {}", userUuid, type);
                });
    }

    @Override
    public Optional<VerificationCode> getActiveVerificationCode(String userUuid, CodeType type) {
        return verificationCodeRepository.findByUserUuidAndTypeAndUsedFalse(userUuid, type)
                .filter(vc -> vc.getExpiresAt().isAfter(LocalDateTime.now()));
    }

    @Override
    @Transactional
    public void resendVerificationCode(String userUuid, String newCode, CodeType type) {
        // Invalidate existing codes
        invalidateExistingCodes(userUuid, type);

        // Save new code with the specified type
        saveVerificationCode(userUuid, newCode, type);

        log.info("Verification code resent for userUuid: {}, type: {}", userUuid, type);
    }

    // ========================================================================
    // RATE LIMITING AND SECURITY
    // ========================================================================

    @Override
    public boolean canRequestNewCode(String userUuid, CodeType type) {
        Optional<VerificationCode> lastCode = verificationCodeRepository
                .findTopByUserUuidAndTypeOrderByCreatedAtDesc(userUuid, type);

        if (lastCode.isEmpty()) {
            return true;
        }

        // Check cooldown period (prevent spam)
        LocalDateTime cooldownEnd = lastCode.get().getCreatedAt().plusMinutes(getCooldownMinutes(type));
        return LocalDateTime.now().isAfter(cooldownEnd);
    }

    @Override
    public long getRemainingCooldownSeconds(String userUuid, CodeType type) {
        Optional<VerificationCode> lastCode = verificationCodeRepository
                .findTopByUserUuidAndTypeOrderByCreatedAtDesc(userUuid, type);

        if (lastCode.isEmpty()) {
            return 0;
        }

        LocalDateTime cooldownEnd = lastCode.get().getCreatedAt().plusMinutes(getCooldownMinutes(type));
        LocalDateTime now = LocalDateTime.now();

        if (now.isAfter(cooldownEnd)) {
            return 0;
        }

        return java.time.Duration.between(now, cooldownEnd).getSeconds();
    }

    // ========================================================================
    // HELPER METHODS
    // ========================================================================

    private int getExpirationMinutes(CodeType type) {
        switch (type) {
            case EMAIL_VERIFICATION:
                return 15;
            case PASSWORD_RESET:
                return 10;
            default:
                return 15;
        }
    }

    private int getCooldownMinutes(CodeType type) {
        switch (type) {
            case EMAIL_VERIFICATION:
                return 1; // 1 minute cooldown
            case PASSWORD_RESET:
                return 2; // 2 minute cooldown for security
            default:
                return 1;
        }
    }
}
