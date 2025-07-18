package com.auth.service.controller;


import com.auth.service.dto.*;
import com.auth.service.exceptions.BadRequestException;
import com.auth.service.service.AuthService;
import com.auth.service.service.VerificationService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("api/auth/")
@Slf4j
@AllArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final VerificationService verificationService;

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<String>> registerUser(@Valid @RequestBody NewUserRecord newUserRecord) {
        try {
            log.info("Received user registration request for email: {}", newUserRecord.email());

            authService.createUser(newUserRecord);

            ApiResponse<String> response = ApiResponse.success(
                    "User registered successfully. Please check your email for verification code.",
                    "Registration completed"
            );

            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (BadRequestException e) {
            log.warn("User registration failed: {}", e.getMessage());
            ApiResponse<String> response = ApiResponse.error(
                    e.getMessage(),
                    HttpStatus.BAD_REQUEST.value()
            );
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);

        } catch (Exception e) {
            log.error("Unexpected error during user registration", e);
            ApiResponse<String> response = ApiResponse.error(
                    "Registration failed due to an internal error. Please try again later.",
                    "INTERNAL_SERVER_ERROR",
                    HttpStatus.INTERNAL_SERVER_ERROR.value()
            );
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }


    @PostMapping("/login")
    public ResponseEntity<ApiResponse<TokenResponse>> login(@RequestBody LoginRequest request) {
        TokenResponse token = authService.login(request.getUsername(), request.getPassword());
        return ResponseEntity.ok(ApiResponse.success(token));
    }

    /**
     * Verify user email with verification code
     */
    @PutMapping("/verify-email")
    public ResponseEntity<ApiResponse<String>> verifyEmail(@Valid @RequestBody VerificationRequestDTO request) {
        try {
            log.info("Received email verification request for userId: {}", request.getUserId());

            authService.verifyEmailWithCode(request.getUserId(), request.getCode());

            ApiResponse<String> response = ApiResponse.success(
                    "Email verified successfully. You can now log in to your account."
            );

            return ResponseEntity.ok(response);

        } catch (BadRequestException e) {
            log.warn("Email verification failed for userId: {} - {}", request.getUserId(), e.getMessage());
            ApiResponse<String> response = ApiResponse.error(
                    e.getMessage(),
                    HttpStatus.BAD_REQUEST.value()
            );
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);

        } catch (Exception e) {
            log.error("Unexpected error during email verification for userId: {}", request.getUserId(), e);
            ApiResponse<String> response = ApiResponse.error(
                    "Email verification failed due to an internal error. Please try again later.",
                    "VERIFICATION_ERROR",
                    HttpStatus.INTERNAL_SERVER_ERROR.value()
            );
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Resend verification code to user's email
     */
    @PostMapping("/resend-verification-code")
    public ResponseEntity<ApiResponse<String>> resendVerificationCode(@Valid @RequestBody ResendCodeRequest request) {
        try {
            log.info("Received resend verification code request for userId: {}", request.email());

            authService.resendVerificationCode(request.email());

            ApiResponse<String> response = ApiResponse.success(
                    "Verification code sent successfully. Please check your email."
            );

            return ResponseEntity.ok(response);

        } catch (BadRequestException e) {
            log.warn("Failed to resend verification code for userId: {} - {}", request.email(), e.getMessage());
            ApiResponse<String> response = ApiResponse.error(
                    e.getMessage(),
                    HttpStatus.BAD_REQUEST.value()
            );
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);

        } catch (Exception e) {
            log.error("Unexpected error while resending verification code for userId: {}", request.email(), e);
            ApiResponse<String> response = ApiResponse.error(
                    "Failed to resend verification code due to an internal error. Please try again later.",
                    "RESEND_FAILED",
                    HttpStatus.INTERNAL_SERVER_ERROR.value()
            );
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }


    @PostMapping("/initiate-password-reset")
    public ResponseEntity<ApiResponse<String>> initiatePasswordReset(@Valid @RequestBody InitiatePasswordResetRequest request) {
        try {
            log.info("Received password reset initiation request for email: {}", request.email());

            authService.initiatePasswordReset(request.email());

            ApiResponse<String> response = ApiResponse.success(
                    "If an account with this email exists, you will receive a password reset code shortly."
            );

            return ResponseEntity.ok(response);

        } catch (BadRequestException e) {
            log.warn("Failed to initiate password reset for email: {} - {}", request.email(), e.getMessage());
            ApiResponse<String> response = ApiResponse.error(
                    e.getMessage(),
                    HttpStatus.BAD_REQUEST.value()
            );
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);

        } catch (Exception e) {
            log.error("Unexpected error while initiating password reset for email: {}", request.email(), e);
            ApiResponse<String> response = ApiResponse.error(
                    "Failed to initiate password reset due to an internal error. Please try again later.",
                    "PASSWORD_RESET_FAILED",
                    HttpStatus.INTERNAL_SERVER_ERROR.value()
            );
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @PostMapping("/verify-reset-password-code")
    public ResponseEntity<ApiResponse<String>> verifyResetPasswordCode(@Valid @RequestBody VerifyResetCodeRequest request) {
        try {
            log.info("Received password reset code verification request for email: {}", request.email());

            authService.verifyResetPasswordCode(request.email(), request.code());

            ApiResponse<String> response = ApiResponse.success(
                    "Password reset code verified successfully. You can now reset your password."
            );

            return ResponseEntity.ok(response);

        } catch (BadRequestException e) {
            log.warn("Failed to verify reset password code for email: {} - {}", request.email(), e.getMessage());
            ApiResponse<String> response = ApiResponse.error(
                    e.getMessage(),
                    HttpStatus.BAD_REQUEST.value()
            );
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);

        } catch (Exception e) {
            log.error("Unexpected error while verifying reset password code for email: {}", request.email(), e);
            ApiResponse<String> response = ApiResponse.error(
                    "Failed to verify reset code due to an internal error. Please try again later.",
                    "PASSWORD_RESET_ERROR",
                    HttpStatus.INTERNAL_SERVER_ERROR.value()
            );
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @PostMapping("/resend-password-reset-code")
    public ResponseEntity<ApiResponse<String>> resendPasswordResetCode(@Valid @RequestBody ResendPasswordResetRequest request) {
        try {
            log.info("Received resend password reset code request for email: {}", request.email());

            authService.resendPasswordResetCode(request.email());

            ApiResponse<String> response = ApiResponse.success(
                    "If an account with this email exists, a new password reset code has been sent."
            );

            return ResponseEntity.ok(response);

        } catch (BadRequestException e) {
            log.warn("Failed to resend password reset code for email: {} - {}", request.email(), e.getMessage());
            ApiResponse<String> response = ApiResponse.error(
                    e.getMessage(),
                    HttpStatus.BAD_REQUEST.value()
            );
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);

        } catch (Exception e) {
            log.error("Unexpected error while resending password reset code for email: {}", request.email(), e);
            ApiResponse<String> response = ApiResponse.error(
                    "Failed to resend password reset code due to an internal error. Please try again later.",
                    "RESEND_FAILED",
                    HttpStatus.INTERNAL_SERVER_ERROR.value()
            );
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @PutMapping("/reset-password")
    public ResponseEntity<ApiResponse<String>> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        try {
            log.info("Received password reset request for userId: {}", request.userId());

            authService.resetPassword(request.userId(), request.newPassword(), request.confirmPassword());

            ApiResponse<String> response = ApiResponse.success(
                    "Password has been reset successfully."
            );

            return ResponseEntity.ok(response);
        } catch (BadRequestException e) {
            log.warn("Failed to reset password for userId: {} - {}", request.userId(), e.getMessage());
            ApiResponse<String> response = ApiResponse.error(
                    e.getMessage(),
                    HttpStatus.BAD_REQUEST.value()
            );
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);

        } catch (Exception e) {
            log.error("Unexpected error while resetting password for userId: {}", request.userId(), e);
            ApiResponse<String> response = ApiResponse.error(
                    "Failed to reset password due to an internal error. Please try again later.",
                    "PASSWORD_UPDATE_FAILED",
                    HttpStatus.INTERNAL_SERVER_ERROR.value()
            );
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

}
