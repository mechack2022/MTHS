package com.mths.prescription.dto;

import com.mths.prescription.entity.PrescriptionItem;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * Response DTO for prescription item (medication)
 */
@Data
@Schema(description = "Prescription item (medication) details")
public class PrescriptionItemResponse {

    @Schema(description = "Prescription item ID", example = "1")
    private Long id;

    @Schema(description = "Medication name", example = "Amoxicillin 500mg")
    private String medicationName;

    @Schema(description = "Generic/chemical name", example = "Amoxicillin")
    private String genericName;

    @Schema(description = "Dosage", example = "500mg")
    private String dosage;

    @Schema(description = "Frequency of intake", example = "Twice daily")
    private String frequency;

    @Schema(description = "Duration of treatment", example = "7 days")
    private String duration;

    @Schema(description = "Route of administration", example = "Oral")
    private String route;

    @Schema(description = "Quantity to dispense", example = "14")
    private Integer quantity;

    @Schema(description = "Unit of measurement", example = "tablets")
    private String unit;

    @Schema(description = "Special instructions", example = "Take with food")
    private String instructions;

    @Schema(description = "Additional special instructions or warnings")
    private String specialInstructions;

    @Schema(description = "Medication timing", example = "AFTER_MEAL")
    private PrescriptionItem.MedicationTiming timing;

    @Schema(description = "Is medication dispensed?", example = "false")
    private Boolean isDispensed;

    @Schema(description = "Quantity dispensed", example = "14")
    private Integer quantityDispensed;

    @Schema(description = "Remaining quantity to dispense", example = "0")
    private Integer remainingQuantity;

    @Schema(description = "Is fully dispensed?", example = "true")
    private Boolean isFullyDispensed;

    @Schema(description = "Record creation timestamp")
    private LocalDateTime createdAt;

    @Schema(description = "Record last update timestamp")
    private LocalDateTime updatedAt;
}
