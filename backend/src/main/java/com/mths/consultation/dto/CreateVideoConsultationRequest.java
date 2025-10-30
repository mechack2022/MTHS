package com.mths.consultation.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CreateVideoConsultationRequest {
    
    @NotNull(message = "Appointment ID is required")
    private Long appointmentId;
    
    @NotNull(message = "Scheduled start time is required")
    private LocalDateTime scheduledStartTime;
    
    private Integer estimatedDuration; // in minutes
    
    private Boolean recordingEnabled = false;
    
    private String notes;
    
    private String meetingTitle;
    
    // Configuration options
    private Boolean allowScreenSharing = true;
    private Boolean allowFileSharing = true;
    private Boolean requireModerator = false;
    private String roomPassword;
    
    // Notification preferences
    private Boolean sendEmailNotifications = true;
    private Boolean sendSmsReminders = false;
    
    // Additional settings
    private String preferredLanguage = "en";
    private String timeZone = "UTC";
}