package com.auth.service.constants;

public enum VerificationResult {
    SUCCESS("Verification successful"),
    CODE_NOT_FOUND("Verification code not found or invalid"),
    CODE_EXPIRED("Verification code has expired"),
    CODE_ALREADY_USED("Verification code has already been used"),
    INVALID_CODE("Invalid verification code"),
    USER_NOT_FOUND("User not found");

    private final String message;

    VerificationResult(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}
