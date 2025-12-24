package com.mths.prescription.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * Request DTO for pharmacist to dispense medication
 */
@Data
@Schema(description = "Request for pharmacist to dispense medication from a prescription")
public class DispenseMedicationRequest {

    @Schema(description = "Pharmacist ID performing the dispensing", example = "111", required = true)
    @NotNull(message = "Pharmacist ID is required")
    private Long pharmacistId;

    @Schema(description = "List of prescription items being dispensed with quantities")
    private List<DispenseItemRequest> items = new ArrayList<>();

    @Schema(description = "Notes from pharmacist about the dispensing", example = "All medications available and dispensed")
    private String pharmacyNotes;

    @Schema(description = "Is this a partial dispensing (not all items/quantities available)?", example = "false")
    private Boolean isPartialDispense = false;

    /**
     * Inner class for individual item dispensing details
     */
    @Data
    @Schema(description = "Details for dispensing a specific prescription item")
    public static class DispenseItemRequest {

        @Schema(description = "Prescription item ID", example = "1", required = true)
        @NotNull(message = "Prescription item ID is required")
        private Long prescriptionItemId;

        @Schema(description = "Quantity being dispensed", example = "14", required = true)
        @NotNull(message = "Quantity dispensed is required")
        @Positive(message = "Quantity must be positive")
        private Integer quantityDispensed;

        @Schema(description = "Notes about this specific item", example = "Generic substitution approved by patient")
        private String itemNotes;
    }
}
