package com.mths.patient.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * Request DTO for creating standalone vital signs (not linked to an appointment)
 * Used for home monitoring, walk-in checks, remote patient monitoring, etc.
 *
 * Note: The recordedByUser will be automatically set from the authenticated user.
 * The recordedByRole will be determined from the user's role in the system.
 */
@Data
public class CreateVitalSignsRequest {

    @NotNull(message = "Patient profile ID is required")
    private Long patientProfileId;

    private String bloodPressure;  // Format: "120/80"
    private Integer heartRate;
    private Double temperature;    // In Celsius
    private Double weight;         // In kg
    private Double height;         // In cm
    private String notes;
}
