package com.mths.prescription.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * Request DTO for sending a prescription to a pharmacy
 */
@Data
@Schema(description = "Request to send prescription to pharmacy for fulfillment")
public class SendToPharmacyRequest {

    @Schema(description = "Pharmacy ID to send prescription to", example = "321", required = true)
    @NotNull(message = "Pharmacy ID is required")
    private Long pharmacyId;

    @Schema(description = "Optional notes for the pharmacy", example = "Patient prefers generic alternatives if available")
    private String notes;

    @Schema(description = "Urgent/priority prescription?", example = "false")
    private Boolean isUrgent = false;
}
