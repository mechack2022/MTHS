package com.mths.consultation.dto;

import com.mths.shared.constants.AppointmentStatus;
import lombok.Data;

@Data
public class UpdateAppointmentRequest {
    
    private AppointmentStatus status;
    private String consultationNotes;
    private String diagnosis;
    private String meetingUrl;
    private String meetingId;
    
    // Vital signs updates
    private String bloodPressure;
    private Integer heartRate;
    private Double temperature;
    private Double weight;
    private Double height;
    private String vitalSignsNotes;
}