package com.mths.prescription.controller;

import com.mths.prescription.dto.CreatePrescriptionRequest;
import com.mths.prescription.dto.DispenseMedicationRequest;
import com.mths.prescription.dto.PrescriptionResponse;
import com.mths.prescription.dto.SendToPharmacyRequest;
import com.mths.prescription.entity.Prescription;
import com.mths.prescription.service.PrescriptionService;
import com.mths.shared.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.net.MalformedURLException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

/**
 * REST Controller for managing prescriptions
 * Handles prescription creation, document management, and pharmacy workflow
 */
@RestController
@RequestMapping("/api/v1/prescriptions")
@Slf4j
@RequiredArgsConstructor
@Tag(name = "Prescription Management", description = "APIs for managing prescriptions and pharmacy workflow")
public class PrescriptionController {

    private final PrescriptionService prescriptionService;

    /**
     * Create a new prescription with document upload
     * Automatically sends to pharmacy for fulfillment
     */
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyRole('DOCTOR', 'ADMIN', 'SUPER_ADMIN')")
    @Operation(summary = "Create prescription", description = "Create a new prescription with document upload. Automatically sent to pharmacy.")
    public ResponseEntity<ApiResponse<PrescriptionResponse>> createPrescription(
            @Parameter(description = "Prescription details") @Valid @RequestPart("request") CreatePrescriptionRequest request,
            @Parameter(description = "Prescription document (PDF or PNG)") @RequestPart("document") MultipartFile document) {

        log.info("Creating prescription for patient: {}, doctor: {}, pharmacy: {}",
                request.getPatientProfileId(), request.getDoctorProfileId(), request.getPharmacyId());

        PrescriptionResponse prescription = prescriptionService.createPrescription(request, document);

        ApiResponse<PrescriptionResponse> response = ApiResponse.success(
                "Prescription created and sent to pharmacy successfully",
                prescription,
                HttpStatus.CREATED.value()
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Get prescription by ID
     */
    @GetMapping("/{prescriptionId}")
    @PreAuthorize("hasAnyRole('PATIENT', 'DOCTOR', 'PHARMACIST', 'ADMIN', 'SUPER_ADMIN')")
    @Operation(summary = "Get prescription by ID", description = "Retrieve prescription details by ID")
    public ResponseEntity<ApiResponse<PrescriptionResponse>> getPrescriptionById(
            @Parameter(description = "Prescription ID") @PathVariable Long prescriptionId) {

        log.info("Retrieving prescription: {}", prescriptionId);

        PrescriptionResponse prescription = prescriptionService.getPrescriptionById(prescriptionId);

        ApiResponse<PrescriptionResponse> response = ApiResponse.success(
                "Prescription retrieved successfully",
                prescription
        );

        return ResponseEntity.ok(response);
    }

    /**
     * Get prescription by prescription number
     */
    @GetMapping("/number/{prescriptionNumber}")
    @PreAuthorize("hasAnyRole('PATIENT', 'DOCTOR', 'PHARMACIST', 'ADMIN', 'SUPER_ADMIN')")
    @Operation(summary = "Get prescription by number", description = "Retrieve prescription by prescription number")
    public ResponseEntity<ApiResponse<PrescriptionResponse>> getPrescriptionByNumber(
            @Parameter(description = "Prescription number") @PathVariable String prescriptionNumber) {

        log.info("Retrieving prescription by number: {}", prescriptionNumber);

        PrescriptionResponse prescription = prescriptionService.getPrescriptionByNumber(prescriptionNumber);

        ApiResponse<PrescriptionResponse> response = ApiResponse.success(
                "Prescription retrieved successfully",
                prescription
        );

        return ResponseEntity.ok(response);
    }

    /**
     * Update prescription document
     */
    @PutMapping(value = "/{prescriptionId}/document", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyRole('DOCTOR', 'ADMIN', 'SUPER_ADMIN')")
    @Operation(summary = "Update prescription document", description = "Update the prescription document (PDF or PNG)")
    public ResponseEntity<ApiResponse<PrescriptionResponse>> updatePrescriptionDocument(
            @Parameter(description = "Prescription ID") @PathVariable Long prescriptionId,
            @Parameter(description = "New prescription document") @RequestPart("document") MultipartFile document) {

        log.info("Updating prescription document for prescription: {}", prescriptionId);

        PrescriptionResponse prescription = prescriptionService.updatePrescriptionDocument(prescriptionId, document);

        ApiResponse<PrescriptionResponse> response = ApiResponse.success(
                "Prescription document updated successfully",
                prescription
        );

        return ResponseEntity.ok(response);
    }

    /**
     * Download prescription document
     */
    @GetMapping("/{prescriptionId}/document")
    @PreAuthorize("hasAnyRole('PATIENT', 'DOCTOR', 'PHARMACIST', 'ADMIN', 'SUPER_ADMIN')")
    @Operation(summary = "Download prescription document", description = "Download the prescription document file")
    public ResponseEntity<ApiResponse<String>> getPrescriptionDocument(
            @Parameter(description = "Prescription ID") @PathVariable Long prescriptionId) {

        log.info("Downloading prescription document for prescription: {}", prescriptionId);

        String documentPath = prescriptionService.getPrescriptionDocumentPath(prescriptionId);

        ApiResponse<String> response = ApiResponse.success(
                "Prescription document URL retrieved successfully",
                documentPath
        );

        return ResponseEntity.ok(response);
    }

    /**
     * Get all prescriptions for a patient
     */
    @GetMapping("/patient/{patientProfileId}")
    @PreAuthorize("hasAnyRole('PATIENT', 'DOCTOR', 'ADMIN', 'SUPER_ADMIN')")
    @Operation(summary = "Get patient prescriptions", description = "Get all prescriptions for a patient")
    public ResponseEntity<ApiResponse<List<PrescriptionResponse>>> getPatientPrescriptions(
            @Parameter(description = "Patient profile ID") @PathVariable Long patientProfileId) {

        log.info("Retrieving prescriptions for patient: {}", patientProfileId);

        List<PrescriptionResponse> prescriptions = prescriptionService.getPatientPrescriptions(patientProfileId);

        ApiResponse<List<PrescriptionResponse>> response = ApiResponse.success(
                "Patient prescriptions retrieved successfully",
                prescriptions
        );

        return ResponseEntity.ok(response);
    }

    /**
     * Get active prescriptions for a patient
     */
    @GetMapping("/patient/{patientProfileId}/active")
    @PreAuthorize("hasAnyRole('PATIENT', 'DOCTOR', 'ADMIN', 'SUPER_ADMIN')")
    @Operation(summary = "Get active patient prescriptions", description = "Get all active prescriptions for a patient")
    public ResponseEntity<ApiResponse<List<PrescriptionResponse>>> getActivePatientPrescriptions(
            @Parameter(description = "Patient profile ID") @PathVariable Long patientProfileId) {

        log.info("Retrieving active prescriptions for patient: {}", patientProfileId);

        List<PrescriptionResponse> prescriptions = prescriptionService.getActivePatientPrescriptions(patientProfileId);

        ApiResponse<List<PrescriptionResponse>> response = ApiResponse.success(
                "Active patient prescriptions retrieved successfully",
                prescriptions
        );

        return ResponseEntity.ok(response);
    }

    /**
     * Get all prescriptions issued by a doctor
     */
    @GetMapping("/doctor/{doctorProfileId}")
    @PreAuthorize("hasAnyRole('DOCTOR', 'ADMIN', 'SUPER_ADMIN')")
    @Operation(summary = "Get doctor prescriptions", description = "Get all prescriptions issued by a doctor")
    public ResponseEntity<ApiResponse<List<PrescriptionResponse>>> getDoctorPrescriptions(
            @Parameter(description = "Doctor profile ID") @PathVariable Long doctorProfileId) {

        log.info("Retrieving prescriptions for doctor: {}", doctorProfileId);

        List<PrescriptionResponse> prescriptions = prescriptionService.getDoctorPrescriptions(doctorProfileId);

        ApiResponse<List<PrescriptionResponse>> response = ApiResponse.success(
                "Doctor prescriptions retrieved successfully",
                prescriptions
        );

        return ResponseEntity.ok(response);
    }

    /**
     * Get prescriptions for an appointment
     */
    @GetMapping("/appointment/{appointmentId}")
    @PreAuthorize("hasAnyRole('PATIENT', 'DOCTOR', 'ADMIN', 'SUPER_ADMIN')")
    @Operation(summary = "Get appointment prescriptions", description = "Get all prescriptions for an appointment")
    public ResponseEntity<ApiResponse<List<PrescriptionResponse>>> getAppointmentPrescriptions(
            @Parameter(description = "Appointment ID") @PathVariable Long appointmentId) {

        log.info("Retrieving prescriptions for appointment: {}", appointmentId);

        List<PrescriptionResponse> prescriptions = prescriptionService.getAppointmentPrescriptions(appointmentId);

        ApiResponse<List<PrescriptionResponse>> response = ApiResponse.success(
                "Appointment prescriptions retrieved successfully",
                prescriptions
        );

        return ResponseEntity.ok(response);
    }

    /**
     * Send prescription to pharmacy (manual - if needed)
     * Note: Prescriptions are automatically sent to pharmacy on creation
     */
    @PostMapping("/{prescriptionId}/send-to-pharmacy")
    @PreAuthorize("hasAnyRole('DOCTOR', 'ADMIN', 'SUPER_ADMIN')")
    @Operation(summary = "Send prescription to pharmacy", description = "Manually send prescription to a different pharmacy")
    public ResponseEntity<ApiResponse<PrescriptionResponse>> sendToPharmacy(
            @Parameter(description = "Prescription ID") @PathVariable Long prescriptionId,
            @Valid @RequestBody SendToPharmacyRequest request) {

        log.info("Sending prescription {} to pharmacy {}", prescriptionId, request.getPharmacyId());

        PrescriptionResponse prescription = prescriptionService.sendToPharmacy(prescriptionId, request);

        ApiResponse<PrescriptionResponse> response = ApiResponse.success(
                "Prescription sent to pharmacy successfully",
                prescription
        );

        return ResponseEntity.ok(response);
    }

    /**
     * Get all prescriptions for a pharmacy
     */
    @GetMapping("/pharmacy/{pharmacyId}")
    @PreAuthorize("hasAnyRole('PHARMACIST', 'ADMIN', 'SUPER_ADMIN')")
    @Operation(summary = "Get pharmacy prescriptions", description = "Get all prescriptions for a pharmacy")
    public ResponseEntity<ApiResponse<List<PrescriptionResponse>>> getPharmacyPrescriptions(
            @Parameter(description = "Pharmacy ID") @PathVariable Long pharmacyId) {

        log.info("Retrieving prescriptions for pharmacy: {}", pharmacyId);

        List<PrescriptionResponse> prescriptions = prescriptionService.getPharmacyPrescriptions(pharmacyId);

        ApiResponse<List<PrescriptionResponse>> response = ApiResponse.success(
                "Pharmacy prescriptions retrieved successfully",
                prescriptions
        );

        return ResponseEntity.ok(response);
    }

    /**
     * Get pharmacy prescriptions by status
     */
    @GetMapping("/pharmacy/{pharmacyId}/status/{status}")
    @PreAuthorize("hasAnyRole('PHARMACIST', 'ADMIN', 'SUPER_ADMIN')")
    @Operation(summary = "Get pharmacy prescriptions by status", description = "Get pharmacy prescriptions filtered by status")
    public ResponseEntity<ApiResponse<List<PrescriptionResponse>>> getPharmacyPrescriptionsByStatus(
            @Parameter(description = "Pharmacy ID") @PathVariable Long pharmacyId,
            @Parameter(description = "Pharmacy status") @PathVariable Prescription.PharmacyStatus status) {

        log.info("Retrieving prescriptions for pharmacy: {} with status: {}", pharmacyId, status);

        List<PrescriptionResponse> prescriptions = prescriptionService.getPharmacyPrescriptionsByStatus(pharmacyId, status);

        ApiResponse<List<PrescriptionResponse>> response = ApiResponse.success(
                "Pharmacy prescriptions by status retrieved successfully",
                prescriptions
        );

        return ResponseEntity.ok(response);
    }

    /**
     * Update pharmacy status
     */
    @PutMapping("/{prescriptionId}/pharmacy-status")
    @PreAuthorize("hasAnyRole('PHARMACIST', 'ADMIN', 'SUPER_ADMIN')")
    @Operation(summary = "Update pharmacy status", description = "Update the pharmacy fulfillment status")
    public ResponseEntity<ApiResponse<PrescriptionResponse>> updatePharmacyStatus(
            @Parameter(description = "Prescription ID") @PathVariable Long prescriptionId,
            @Parameter(description = "New pharmacy status") @RequestParam Prescription.PharmacyStatus status,
            @Parameter(description = "Optional pharmacy notes") @RequestParam(required = false) String pharmacyNotes) {

        log.info("Updating pharmacy status for prescription {} to {}", prescriptionId, status);

        PrescriptionResponse prescription = prescriptionService.updatePharmacyStatus(prescriptionId, status, pharmacyNotes);

        ApiResponse<PrescriptionResponse> response = ApiResponse.success(
                "Pharmacy status updated successfully",
                prescription
        );

        return ResponseEntity.ok(response);
    }

    /**
     * Dispense medication to patient
     */
    @PostMapping("/{prescriptionId}/dispense")
    @PreAuthorize("hasAnyRole('PHARMACIST', 'ADMIN', 'SUPER_ADMIN')")
    @Operation(summary = "Dispense medication", description = "Mark medication as dispensed and delivered to patient")
    public ResponseEntity<ApiResponse<PrescriptionResponse>> dispenseMedication(
            @Parameter(description = "Prescription ID") @PathVariable Long prescriptionId,
            @Valid @RequestBody DispenseMedicationRequest request) {

        log.info("Dispensing medication for prescription: {}", prescriptionId);

        PrescriptionResponse prescription = prescriptionService.dispenseMedication(prescriptionId, request);

        ApiResponse<PrescriptionResponse> response = ApiResponse.success(
                "Medication dispensed successfully",
                prescription
        );

        return ResponseEntity.ok(response);
    }

    /**
     * Get refillable prescriptions for a patient
     */
    @GetMapping("/patient/{patientProfileId}/refillable")
    @PreAuthorize("hasAnyRole('PATIENT', 'DOCTOR', 'ADMIN', 'SUPER_ADMIN')")
    @Operation(summary = "Get refillable prescriptions", description = "Get all refillable prescriptions for a patient")
    public ResponseEntity<ApiResponse<List<PrescriptionResponse>>> getRefillablePrescriptions(
            @Parameter(description = "Patient profile ID") @PathVariable Long patientProfileId) {

        log.info("Retrieving refillable prescriptions for patient: {}", patientProfileId);

        List<PrescriptionResponse> prescriptions = prescriptionService.getRefillablePrescriptions(patientProfileId);

        ApiResponse<List<PrescriptionResponse>> response = ApiResponse.success(
                "Refillable prescriptions retrieved successfully",
                prescriptions
        );

        return ResponseEntity.ok(response);
    }

    /**
     * Create a refill prescription
     */
    @PostMapping(value = "/{prescriptionId}/refill", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyRole('DOCTOR', 'ADMIN', 'SUPER_ADMIN')")
    @Operation(summary = "Create refill prescription", description = "Create a refill for an existing prescription")
    public ResponseEntity<ApiResponse<PrescriptionResponse>> createRefill(
            @Parameter(description = "Original prescription ID") @PathVariable Long prescriptionId,
            @Parameter(description = "New prescription document") @RequestPart("document") MultipartFile document) {

        log.info("Creating refill for prescription: {}", prescriptionId);

        PrescriptionResponse prescription = prescriptionService.createRefill(prescriptionId, document);

        ApiResponse<PrescriptionResponse> response = ApiResponse.success(
                "Refill prescription created successfully",
                prescription,
                HttpStatus.CREATED.value()
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Cancel prescription
     */
    @PutMapping("/{prescriptionId}/cancel")
    @PreAuthorize("hasAnyRole('DOCTOR', 'ADMIN', 'SUPER_ADMIN')")
    @Operation(summary = "Cancel prescription", description = "Cancel an active prescription")
    public ResponseEntity<ApiResponse<PrescriptionResponse>> cancelPrescription(
            @Parameter(description = "Prescription ID") @PathVariable Long prescriptionId,
            @Parameter(description = "Cancellation reason") @RequestParam String reason) {

        log.info("Cancelling prescription: {}", prescriptionId);

        PrescriptionResponse prescription = prescriptionService.cancelPrescription(prescriptionId, reason);

        ApiResponse<PrescriptionResponse> response = ApiResponse.success(
                "Prescription cancelled successfully",
                prescription
        );

        return ResponseEntity.ok(response);
    }

    /**
     * Expire prescription
     */
    @PutMapping("/{prescriptionId}/expire")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    @Operation(summary = "Expire prescription", description = "Mark prescription as expired")
    public ResponseEntity<ApiResponse<PrescriptionResponse>> expirePrescription(
            @Parameter(description = "Prescription ID") @PathVariable Long prescriptionId) {

        log.info("Expiring prescription: {}", prescriptionId);

        PrescriptionResponse prescription = prescriptionService.expirePrescription(prescriptionId);

        ApiResponse<PrescriptionResponse> response = ApiResponse.success(
                "Prescription expired successfully",
                prescription
        );

        return ResponseEntity.ok(response);
    }

    /**
     * Get prescriptions ready for pickup
     */
    @GetMapping("/ready-for-pickup")
    @PreAuthorize("hasAnyRole('PHARMACIST', 'ADMIN', 'SUPER_ADMIN')")
    @Operation(summary = "Get prescriptions ready for pickup", description = "Get all prescriptions ready for patient pickup")
    public ResponseEntity<ApiResponse<List<PrescriptionResponse>>> getPrescriptionsReadyForPickup() {

        log.info("Retrieving prescriptions ready for pickup");

        List<PrescriptionResponse> prescriptions = prescriptionService.getPrescriptionsReadyForPickup();

        ApiResponse<List<PrescriptionResponse>> response = ApiResponse.success(
                "Prescriptions ready for pickup retrieved successfully",
                prescriptions
        );

        return ResponseEntity.ok(response);
    }

    /**
     * Get pending pharmacy prescriptions
     */
    @GetMapping("/pending")
    @PreAuthorize("hasAnyRole('PHARMACIST', 'ADMIN', 'SUPER_ADMIN')")
    @Operation(summary = "Get pending pharmacy prescriptions", description = "Get all prescriptions pending at pharmacy")
    public ResponseEntity<ApiResponse<List<PrescriptionResponse>>> getPendingPharmacyPrescriptions() {

        log.info("Retrieving pending pharmacy prescriptions");

        List<PrescriptionResponse> prescriptions = prescriptionService.getPendingPharmacyPrescriptions();

        ApiResponse<List<PrescriptionResponse>> response = ApiResponse.success(
                "Pending pharmacy prescriptions retrieved successfully",
                prescriptions
        );

        return ResponseEntity.ok(response);
    }
}
