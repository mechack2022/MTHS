package com.mths.shared.constants;

public enum VerificationStatus {
    PENDING("Pending admin verification"),
    APPROVED("Verified by administrator"),
    REJECTED("Rejected by administrator");

    private final String description;

    VerificationStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
