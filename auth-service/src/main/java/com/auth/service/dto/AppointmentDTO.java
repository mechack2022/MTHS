package com.auth.service.dto;

import com.auth.service.constants.AppointmentStatus;
import com.auth.service.entity.Appointment;
import com.auth.service.entity.VitalSigns;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AppointmentDTO {
    private Long id;
    private Long patientProfileId;
    private Long doctorProfileId;
    private Appointment.AppointmentType appointmentType;
    private LocalDateTime scheduledDatetime;
    private AppointmentStatus status;
    private String consultationNotes;
    private String symptoms;
    private String diagnosis;
    private Double consultationFee;
    private Integer durationMinutes;
    private String meetingUrl;
    private String meetingId;
    private String cancelledReason;
    private String cancelledBy;
    private LocalDateTime cancelledAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // Patient and Doctor details
    private String patientName;
    private String patientEmail;
    private String doctorName;
    private String doctorSpecialization;
    
    // Vital signs
    private VitalSignsDTO vitalSigns;
    
    // Helper fields
    private boolean canBeRescheduled;
    private boolean canBeCancelled;
}