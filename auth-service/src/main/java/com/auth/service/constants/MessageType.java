package com.auth.service.constants;

public enum MessageType {
    TEXT("Text Message"),
    IMAGE("Image"),
    FILE("File"),
    AUDIO("Audio"),
    VIDEO("Video"),
    SYSTEM("System Message"),
    PRESCRIPTION("Prescription"),
    MEDICAL_RECORD("Medical Record");

    private final String description;

    MessageType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}