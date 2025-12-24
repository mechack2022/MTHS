package com.mths.prescription.dto;

import com.mths.prescription.entity.Prescription;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Response DTO for prescription
 */
@Data
@Schema(description = "Prescription details")
public class PrescriptionResponse {

    @Schema(description = "Prescription ID", example = "1")
    private Long id;

    @Schema(description = "Prescription reference number", example = "RX-123-20241213")
    private String prescriptionNumber;

    @Schema(description = "Patient profile ID", example = "123")
    private Long patientProfileId;

    @Schema(description = "Patient full name", example = "John Doe")
    private String patientName;

    @Schema(description = "Doctor profile ID", example = "456")
    private Long doctorProfileId;

    @Schema(description = "Doctor full name", example = "Dr. Jane Smith")
    private String doctorName;

    @Schema(description = "Appointment ID (if created during consultation)", example = "789")
    private Long appointmentId;

    @Schema(description = "Pharmacy ID (selected/recommended pharmacy)", example = "321")
    private Long pharmacyId;

    @Schema(description = "Pharmacy name", example = "HealthPlus Pharmacy")
    private String pharmacyName;

    @Schema(description = "Pharmacist ID (who dispensed medication)", example = "111")
    private Long pharmacistId;

    @Schema(description = "Pharmacist full name", example = "Pharm. Michael Johnson")
    private String pharmacistName;

    @Schema(description = "Creation context", example = "DURING_CONSULTATION")
    private Prescription.CreationContext creationContext;

    @Schema(description = "Diagnosis or medical condition", example = "Upper Respiratory Tract Infection")
    private String diagnosis;

    @Schema(description = "Additional notes", example = "Patient advised to rest")
    private String notes;

    @Schema(description = "Date prescription was issued", example = "2024-12-13")
    private LocalDate issuedDate;

    @Schema(description = "Prescription expiration date", example = "2025-01-13")
    private LocalDate validUntil;

    @Schema(description = "Prescription status", example = "ACTIVE")
    private Prescription.PrescriptionStatus status;

    @Schema(description = "Pharmacy fulfillment status", example = "PENDING")
    private Prescription.PharmacyStatus pharmacyStatus;

    @Schema(description = "Date/time medication was dispensed", example = "2024-12-14T10:30:00")
    private LocalDateTime dispensedDate;

    @Schema(description = "Pharmacist name who dispensed (legacy field)", example = "Pharm. Sarah Lee")
    private String dispensedBy;

    @Schema(description = "Date/time prescription was sent to pharmacy", example = "2024-12-13T15:00:00")
    private LocalDateTime sentToPharmacyAt;

    @Schema(description = "Notes from pharmacist", example = "All medications available and dispensed")
    private String pharmacyNotes;

    @Schema(description = "Is prescription refillable?", example = "false")
    private Boolean isRefillable;

    @Schema(description = "Total refills allowed", example = "2")
    private Integer refillCount;

    @Schema(description = "Remaining refills", example = "2")
    private Integer refillsRemaining;

    @Schema(description = "Is part of ongoing treatment?", example = "false")
    private Boolean isOngoingTreatment;

    @Schema(description = "Previous prescription ID (if this is a refill)", example = "555")
    private Long previousPrescriptionId;

    @Schema(description = "List of medications in prescription")
    private List<PrescriptionItemResponse> medications = new ArrayList<>();

    @Schema(description = "Is prescription expired?", example = "false")
    private Boolean isExpired;

    @Schema(description = "Can prescription be dispensed?", example = "true")
    private Boolean canBeDispensed;

    @Schema(description = "Can prescription be refilled?", example = "false")
    private Boolean canBeRefilled;

    @Schema(description = "Is prescription sent to pharmacy?", example = "true")
    private Boolean isSentToPharmacy;

    @Schema(description = "Is medication ready for pickup?", example = "false")
    private Boolean isReadyForPickup;

    @Schema(description = "Record creation timestamp")
    private LocalDateTime createdAt;

    @Schema(description = "Record last update timestamp")
    private LocalDateTime updatedAt;

    @Schema(description = "Created by (user ID or system)", example = "doctor_456")
    private String createdBy;
}
