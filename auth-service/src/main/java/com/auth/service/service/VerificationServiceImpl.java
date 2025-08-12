package com.auth.service.service;


import com.auth.service.constants.CodeType;
import com.auth.service.constants.Medium;
import com.auth.service.constants.VerificationResult;
import com.auth.service.entity.VerificationCode;
import com.auth.service.exceptions.BadRequestException;
import com.auth.service.repository.VerificationCodeRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@AllArgsConstructor
@Slf4j
public class VerificationServiceImpl implements VerificationService {

    private final VerificationCodeRepository verificationCodeRepository;

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
        if (!verificationCode.getCode().equals(inputCode)) {
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
    public void validateAndThrowIfInvalid(String userUuid, String inputCode, CodeType type) {
        VerificationResult result = validateVerificationCode(userUuid, inputCode, type);

        switch (result) {
            case SUCCESS:
                return; // Valid code, proceed

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
}
