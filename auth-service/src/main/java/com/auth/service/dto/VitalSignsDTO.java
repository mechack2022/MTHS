package com.auth.service.dto;

import com.auth.service.entity.VitalSigns;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class VitalSignsDTO {
    private Long id;
    private Long appointmentId;
    private String bloodPressure;
    private Integer heartRate;
    private Double temperature;
    private Double weight;
    private Double height;
    private Double bmi;
    private LocalDateTime recordedAt;
    private String recordedBy;
    private String notes;
    private VitalSigns.HealthStatus healthStatus;
    
    // Helper fields for frontend display
    private String healthStatusDescription;
    private String healthStatusColor;
    private Integer systolicPressure;
    private Integer diastolicPressure;
}