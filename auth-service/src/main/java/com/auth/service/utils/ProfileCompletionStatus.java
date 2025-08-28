package com.auth.service.utils;

import java.util.List;

public class ProfileCompletionStatus {
    private final boolean complete;
    private final String message;
    private final List<String> missingFields;

    public ProfileCompletionStatus(boolean complete, String message, List<String> missingFields) {
        this.complete = complete;
        this.message = message;
        this.missingFields = missingFields;
    }

    // Getters
    public boolean isComplete() { return complete; }
    public String getMessage() { return message; }
    public List<String> getMissingFields() { return missingFields; }
}

