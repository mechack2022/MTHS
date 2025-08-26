package com.auth.service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VerifyResetCodeResponse {
    private String resetToken;
    private String message;
    private long tokenExpiresInMinutes;

    public static VerifyResetCodeResponse success(String resetToken) {
        return new VerifyResetCodeResponse(
                resetToken,
                "Password reset code verified successfully. Use the provided token to reset your password.",
                15L // 15 minutes token validity
        );
    }
}