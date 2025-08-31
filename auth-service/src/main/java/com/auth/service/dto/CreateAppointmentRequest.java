package com.auth.service.dto;

import com.auth.service.entity.Appointment;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CreateAppointmentRequest {
    
    @NotNull(message = "Patient profile ID is required")
    private Long patientProfileId;
    
    @NotNull(message = "Doctor profile ID is required")
    private Long doctorProfileId;
    
    @NotNull(message = "Appointment type is required")
    private Appointment.AppointmentType appointmentType;
    
    @NotNull(message = "Scheduled datetime is required")
    @Future(message = "Appointment must be scheduled for a future date")
    private LocalDateTime scheduledDatetime;
    
    @Positive(message = "Duration must be positive")
    private Integer durationMinutes = 30;
    
    private String symptoms;
    
    private Double consultationFee;
    
    // Vital Signs - Required for appointment creation
    @Pattern(regexp = "^\\d{2,3}/\\d{2,3}$", message = "Blood pressure must be in format like '120/80'")
    private String bloodPressure;
    
    @Positive(message = "Heart rate must be positive")
    private Integer heartRate;
    
    @Positive(message = "Temperature must be positive")
    private Double temperature;
    
    @Positive(message = "Weight must be positive")
    private Double weight;
    
    @Positive(message = "Height must be positive")
    private Double height;
    
    private String vitalSignsNotes;
}