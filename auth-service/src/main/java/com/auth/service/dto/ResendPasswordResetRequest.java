package com.auth.service.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record ResendPasswordResetRequest(
        @Email(message = "Please provide a valid email address")
        @NotBlank(message = "Email is required")
        String email
) {}
