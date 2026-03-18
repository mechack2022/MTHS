package com.mths.patient.dto;

import com.mths.patient.entity.VitalSigns;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class VitalSignsDTO {
    private Long id;
    private Long patientProfileId;  // Always present
    private Long appointmentId;      // Optional (null for standalone vitals)
    private String bloodPressure;
    private Integer heartRate;
    private Double temperature;
    private Double weight;
    private Double height;
    private Double bmi;
    private LocalDateTime recordedAt;

    // Who recorded these vitals
    private VitalSigns.RecorderRole recordedByRole;  // PATIENT, DOCTOR, NURSE, etc.
    private String recordedByUserId;                  // ID of user who created it
    private String recordedByName;                    // Name of user who created it

    private String notes;
    private VitalSigns.HealthStatus healthStatus;
    
    // Helper fields for frontend display
    private String healthStatusDescription;
    private String healthStatusColor;
    private Integer systolicPressure;
    private Integer diastolicPressure;
}