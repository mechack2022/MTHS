package com.auth.service.controller;


import com.auth.service.constants.VerificationResult;
import com.auth.service.dto.*;
import com.auth.service.dto.UserRegistrationResponse;
import com.auth.service.exceptions.BadRequestException;
import com.auth.service.service.UserAuthService;
import com.auth.service.service.VerificationService;
import com.auth.service.service.ProfileCreationTokenService;
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

    private final UserAuthService authService;
    private final VerificationService verificationService;
    private final ProfileCreationTokenService profileCreationTokenService;

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<UserRegistrationResponse>> registerUser(@Valid @RequestBody UserDTO newUserRecord) {
            log.info("Received user registration request for email: {}", newUserRecord.getEmail());
            UserDTO createdUser = authService.createUser(newUserRecord);
            
            UserRegistrationResponse responseData = new UserRegistrationResponse(
                createdUser.getUuid(),
                createdUser.getEmail()
            );
            
            ApiResponse<UserRegistrationResponse> response = ApiResponse.success(
                    "User registered successfully. Please check your email for verification code.",
                    responseData,
                    HttpStatus.CREATED.value()
            );
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
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
    public ResponseEntity<ApiResponse<VerificationResult>> verifyEmail(@Valid @RequestBody VerificationRequestDTO request) {
           VerificationResult res = authService.verifyEmailWithCode(request.getUserId(), request.getCode());
            ApiResponse<VerificationResult> response = ApiResponse.success(
                    "Email verified successfully",
                    res,
                    HttpStatus.OK.value()
            );
            return ResponseEntity.ok(response);
    }

    @PostMapping("/resend-verification-code")
    public ResponseEntity<ApiResponse<String>> resendVerificationCode(@Valid @RequestBody ResendCodeRequest request) {
        String res = authService.resendEmailVerificationCode(request.email());
        ApiResponse<String> response = ApiResponse.success(
                res,
                request.email(),
                HttpStatus.OK.value()
        );
        return ResponseEntity.ok(response);
    }


    @PostMapping("/initiate-password-reset")
    public ResponseEntity<ApiResponse<String>> initiatePasswordReset(@Valid @RequestBody InitiatePasswordResetRequest request) {
        log.info("Received password reset initiation request for email: {}", request.email());
        
        authService.initiatePasswordReset(request.email());
        
        ApiResponse<String> response = ApiResponse.success(
                "If an account with this email exists, you will receive a password reset code shortly."
        );
        return ResponseEntity.ok(response);
    }

    @PostMapping("/verify-reset-password-code")
    public ResponseEntity<ApiResponse<VerifyResetCodeResponse>> verifyResetPasswordCode(@Valid @RequestBody VerifyResetCodeRequest request) {
        log.info("Received password reset code verification request for email: {}", request.email());
        
        String resetToken = authService.verifyResetPasswordCode(request.email(), request.code());
        VerifyResetCodeResponse responseData = VerifyResetCodeResponse.success(resetToken);
        
        ApiResponse<VerifyResetCodeResponse> response = ApiResponse.success(
                "Password reset code verified successfully. Use the provided token to reset your password.",
                responseData,
                HttpStatus.OK.value()
        );
        return ResponseEntity.ok(response);
    }

    @PostMapping("/resend-password-reset-code")
    public ResponseEntity<ApiResponse<String>> resendPasswordResetCode(@Valid @RequestBody ResendPasswordResetRequest request) {
        log.info("Received resend password reset code request for email: {}", request.email());
        
        authService.resendPasswordResetCode(request.email());
        
        ApiResponse<String> response = ApiResponse.success(
                "If an account with this email exists, a new password reset code has been sent."
        );
        return ResponseEntity.ok(response);
    }

    @PutMapping("/reset-password")
    public ResponseEntity<ApiResponse<String>> resetPassword(@Valid @RequestBody ResetPasswordWithTokenRequest request) {
        log.info("Received password reset request with token");
        
        String result = authService.resetPasswordWithToken(
                request.getResetToken(), 
                request.getNewPassword(), 
                request.getConfirmPassword()
        );
        
        ApiResponse<String> response = ApiResponse.success(result);
        return ResponseEntity.ok(response);
    }

    // ========================================================================
    // PROFILE CREATION TOKEN ENDPOINTS
    // ========================================================================

    @PostMapping("/generate-profile-token")
    public ResponseEntity<ApiResponse<ProfileCreationTokenResponse>> generateProfileCreationToken(
            @Valid @RequestBody GenerateProfileTokenRequest request) {
        
        log.info("Generating profile creation token for user: {}", request.userId());
        
        String token = profileCreationTokenService.generateProfileCreationToken(request.userId());
        
        ProfileCreationTokenResponse tokenResponse = new ProfileCreationTokenResponse(
            token,
            "Token valid for 24 hours. Use this token to create your profile."
        );
        
        ApiResponse<ProfileCreationTokenResponse> response = ApiResponse.success(
            "Profile creation token generated successfully", 
            tokenResponse,
            HttpStatus.OK.value()
        );
        
        return ResponseEntity.ok(response);
    }

    @GetMapping("/profile-creation-status/{userId}")
    public ResponseEntity<ApiResponse<UserVerificationStatusResponse>> getProfileCreationStatus(
            @PathVariable String userId) {
        
        log.info("Checking profile creation status for user: {}", userId);
        
        ProfileCreationTokenService.UserVerificationStatus status = 
                profileCreationTokenService.getUserVerificationStatus(userId);
        
        UserVerificationStatusResponse statusResponse = new UserVerificationStatusResponse(
            status.emailVerified,
            status.accountVerified,
            status.isActive,
            status.verificationStatus.getDescription(),
            profileCreationTokenService.canUserCreateProfile(userId)
        );
        
        ApiResponse<UserVerificationStatusResponse> response = ApiResponse.success(
            "User verification status retrieved", 
            statusResponse,
            HttpStatus.OK.value()
        );
        
        return ResponseEntity.ok(response);
    }

    // ========================================================================
    // REQUEST/RESPONSE RECORDS
    // ========================================================================

    public record GenerateProfileTokenRequest(String userId) {}
    
    public record ProfileCreationTokenResponse(String token, String message) {}
    
    public record UserVerificationStatusResponse(
        boolean emailVerified,
        boolean accountVerified, 
        boolean isActive,
        String verificationStatus,
        boolean canCreateProfile
    ) {}

}
