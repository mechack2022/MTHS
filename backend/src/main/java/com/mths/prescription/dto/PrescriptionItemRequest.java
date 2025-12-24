package com.mths.prescription.dto;

import com.mths.prescription.entity.PrescriptionItem;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

/**
 * Request DTO for creating a prescription item (medication)
 */
@Data
@Schema(description = "Request to add a medication item to a prescription")
public class PrescriptionItemRequest {

    @Schema(description = "Medication name", example = "Amoxicillin 500mg", required = true)
    @NotBlank(message = "Medication name is required")
    private String medicationName;

    @Schema(description = "Generic/chemical name", example = "Amoxicillin")
    private String genericName;

    @Schema(description = "Dosage", example = "500mg", required = true)
    @NotBlank(message = "Dosage is required")
    private String dosage;

    @Schema(description = "Frequency of intake", example = "Twice daily", required = true)
    @NotBlank(message = "Frequency is required")
    private String frequency;

    @Schema(description = "Duration of treatment", example = "7 days", required = true)
    @NotBlank(message = "Duration is required")
    private String duration;

    @Schema(description = "Route of administration", example = "Oral")
    private String route;

    @Schema(description = "Quantity to dispense", example = "14", required = true)
    @NotNull(message = "Quantity is required")
    @Positive(message = "Quantity must be positive")
    private Integer quantity;

    @Schema(description = "Unit of measurement", example = "tablets")
    private String unit;

    @Schema(description = "Special instructions", example = "Take with food")
    private String instructions;

    @Schema(description = "Additional special instructions or warnings", example = "Avoid alcohol while on this medication")
    private String specialInstructions;

    @Schema(description = "Medication timing", example = "AFTER_MEAL",
            allowableValues = {"BEFORE_MEAL", "AFTER_MEAL", "WITH_MEAL", "EMPTY_STOMACH", "BEDTIME", "AS_NEEDED", "MORNING", "EVENING"})
    private PrescriptionItem.MedicationTiming timing;
}
