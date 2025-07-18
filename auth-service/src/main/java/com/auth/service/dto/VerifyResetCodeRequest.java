package com.auth.service.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record VerifyResetCodeRequest(
        @Email(message = "Please provide a valid email address")
        @NotBlank(message = "Email is required")
        String email,

        @NotBlank(message = "Reset code is required")
        @Size(min = 5, max = 5, message = "Reset code must be 6 digits")
        String code
) {}
