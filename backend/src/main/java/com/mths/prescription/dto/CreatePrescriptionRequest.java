package com.mths.prescription.dto;

import com.mths.prescription.entity.Prescription;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.LocalDate;

/**
 * Request DTO for creating a prescription
 * Supports both creation contexts: during consultation and from records
 */
@Data
@Schema(description = "Request to create a new prescription")
public class CreatePrescriptionRequest {

    @Schema(description = "Patient profile ID", example = "123", required = true)
    @NotNull(message = "Patient profile ID is required")
    private Long patientProfileId;

    @Schema(description = "Doctor profile ID", example = "456", required = true)
    @NotNull(message = "Doctor profile ID is required")
    private Long doctorProfileId;

    @Schema(description = "Appointment ID (required for DURING_CONSULTATION context)", example = "789")
    private Long appointmentId;

    @Schema(description = "Pharmacy ID (pharmacy that will fulfill and deliver prescription)", example = "321", required = true)
    @NotNull(message = "Pharmacy ID is required - pharmacy will pick up document and deliver to patient")
    private Long pharmacyId;

    @Schema(description = "Creation context", example = "DURING_CONSULTATION", required = true,
            allowableValues = {"DURING_CONSULTATION", "FROM_RECORDS", "REFILL", "EMERGENCY"})
    @NotNull(message = "Creation context is required")
    private Prescription.CreationContext creationContext;

    @Schema(description = "Diagnosis or medical condition being treated", example = "Upper Respiratory Tract Infection")
    private String diagnosis;

    @Schema(description = "Additional notes or instructions", example = "Patient advised to rest and increase fluid intake")
    private String notes;

    @Schema(description = "Date prescription is issued (defaults to today)", example = "2024-12-13")
    private LocalDate issuedDate;

    @Schema(description = "Expiration date of prescription (auto-calculated if not provided)", example = "2025-01-13")
    private LocalDate validUntil;

    @Schema(description = "Is this prescription refillable?", example = "false")
    private Boolean isRefillable = false;

    @Schema(description = "Number of refills allowed (if refillable)", example = "2")
    @Min(value = 0, message = "Refill count cannot be negative")
    @Max(value = 12, message = "Refill count cannot exceed 12")
    private Integer refillCount = 0;

    @Schema(description = "Is this part of ongoing treatment?", example = "false")
    private Boolean isOngoingTreatment = false;

    @Schema(description = "Previous prescription ID (if this is a refill)", example = "555")
    private Long previousPrescriptionId;

    // NOTE: Prescription document (PDF/PNG) will be uploaded separately via MultipartFile parameter
    // The document should contain all prescription item/medication details

    // Custom validation
    @AssertTrue(message = "Appointment ID is required for DURING_CONSULTATION context")
    private boolean isAppointmentIdValidForContext() {
        if (creationContext == Prescription.CreationContext.DURING_CONSULTATION) {
            return appointmentId != null;
        }
        return true;
    }

    @AssertTrue(message = "Refill count must be greater than 0 if prescription is refillable")
    private boolean isRefillCountValid() {
        if (Boolean.TRUE.equals(isRefillable)) {
            return refillCount != null && refillCount > 0;
        }
        return true;
    }

    @AssertTrue(message = "Previous prescription ID is required for REFILL context")
    private boolean isPreviousPrescriptionValidForContext() {
        if (creationContext == Prescription.CreationContext.REFILL) {
            return previousPrescriptionId != null;
        }
        return true;
    }
}
