package com.auth.service.service;


import com.auth.service.dto.NewUserRecord;
import com.auth.service.dto.TokenResponse;

public interface AuthService {
    void createUser(NewUserRecord newUserRecord);

    TokenResponse login(String username, String password);

    void verifyEmailWithCode(String userId, String code);

    void resendVerificationCode(String userId, String email);

    void resendVerificationCode(String email);

    void initiatePasswordReset(String email);

    void verifyResetPasswordCode(String email, String code);

    void resendPasswordResetCode(String email);

    void resetPassword(String userId, String newPassword, String confirmPassword);

    void markEmailAsVerified(String userId);
}
