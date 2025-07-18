package com.auth.service.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ResetPasswordRequest(
        @NotBlank(message = "User ID is required")
        String userId,

        @NotBlank(message = "New password is required")
        @Size(min = 8, message = "Password must be at least 8 characters long")
        String newPassword,

        @NotBlank(message = "Confirm password is required")
        String confirmPassword
) {}
