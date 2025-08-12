package com.auth.service.service;

import com.auth.service.dto.TokenResponse;
import com.auth.service.dto.UserDTO;
import com.auth.service.entity.User;

public interface UserAuthService {

    UserDTO createUser(UserDTO req);

    TokenResponse login(String username, String password);

    void logout(String refreshToken);

    void verifyEmailWithCode(String userUuid, String code);

    void resendVerificationCode(String userUuid, String email);

    void resendVerificationCode(String email);

    void initiatePasswordReset(String email);

    void verifyResetPasswordCode(String email, String code);

    void resendPasswordResetCode(String email);

    String resetPassword(String userUuid, String newPassword, String confirmPassword);

    void markEmailAsVerified(String userUuid);
}
