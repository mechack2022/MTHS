package com.auth.service.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record ResendCodeRequest(
//        @NotBlank(message = "User ID is required")
//        String userId,

        @NotBlank(message = "Email is required")
        @Email(message = "Please provide a valid email address")
        String email
) {}
