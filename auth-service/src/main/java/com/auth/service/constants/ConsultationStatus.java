package com.auth.service.constants;

public enum ConsultationStatus {
    SCHEDULED("Scheduled"),
    WAITING("Waiting for participants"),
    IN_PROGRESS("In Progress"),
    COMPLETED("Completed"),
    CANCELLED("Cancelled"),
    TECHNICAL_ISSUES("Technical Issues"),
    NO_SHOW("No Show");

    private final String description;

    ConsultationStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}