package com.mths.consultation.dto;

import com.mths.consultation.entity.Appointment;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Schema(description = "Request to create an appointment booking")
public class CreateAppointmentRequest {
    
    @Schema(description = "Patient profile ID", example = "123", required = true)
    @NotNull(message = "Patient profile ID is required")
    private Long patientProfileId;

    @Schema(description = "Doctor profile ID", example = "456", required = true)
    @NotNull(message = "Doctor profile ID is required")
    private Long doctorProfileId;

    @Schema(description = "Type of appointment", example = "VIRTUAL", required = true, allowableValues = {"VIRTUAL", "IN_PERSON"})
    @NotNull(message = "Appointment type is required")
    private Appointment.AppointmentType appointmentType;

    @Schema(description = "Scheduled date and time for appointment (ISO 8601 format)",
            example = "2024-12-15T10:00:00", required = true)
    @NotNull(message = "Scheduled datetime is required")
    @Future(message = "Appointment must be scheduled for a future date")
    private LocalDateTime scheduledDatetime;

    @Schema(description = "Duration of appointment in minutes", example = "30", defaultValue = "30")
    @Positive(message = "Duration must be positive")
    private Integer durationMinutes = 30;

    @Schema(description = "Patient's symptoms or reason for appointment", example = "Chest pain and shortness of breath")
    private String symptoms;

    @Schema(description = "Consultation fee (leave null to use default flat fee)", example = "10000.00")
    private Double consultationFee;
    
    // Vital Signs - Required for appointment creation
    @Schema(description = "Blood pressure reading (systolic/diastolic)", example = "120/80")
    @Pattern(regexp = "^\\d{2,3}/\\d{2,3}$", message = "Blood pressure must be in format like '120/80'")
    private String bloodPressure;

    @Schema(description = "Heart rate in beats per minute", example = "72")
    @Positive(message = "Heart rate must be positive")
    private Integer heartRate;

    @Schema(description = "Body temperature in Celsius", example = "36.5")
    @Positive(message = "Temperature must be positive")
    private Double temperature;

    @Schema(description = "Weight in kilograms", example = "70.5")
    @Positive(message = "Weight must be positive")
    private Double weight;

    @Schema(description = "Height in centimeters", example = "175.0")
    @Positive(message = "Height must be positive")
    private Double height;

    @Schema(description = "Additional notes about vital signs", example = "Patient appears stable")
    private String vitalSignsNotes;
}