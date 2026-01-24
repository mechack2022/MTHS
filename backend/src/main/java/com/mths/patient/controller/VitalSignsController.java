package com.mths.patient.controller;

import com.mths.patient.dto.CreateVitalSignsRequest;
import com.mths.patient.dto.VitalSignsDTO;
import com.mths.patient.service.VitalSignsServiceImpl;
import com.mths.shared.dto.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller for managing standalone vital signs
 * (Vitals not linked to appointments - for home monitoring, walk-in checks, etc.)
 */
@RestController
@RequestMapping("/api/v1/vitals")
@Slf4j
@RequiredArgsConstructor
public class VitalSignsController {

    private final VitalSignsServiceImpl vitalSignsService;

    /**
     * Create standalone vital signs
     * Can be called by: Patient (self-recording), Doctor, Nurse, Lab Tech, Pharmacist
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('PATIENT', 'DOCTOR', 'NURSE', 'LAB_TECHNICIAN', 'PHARMACIST', 'ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<VitalSignsDTO>> createVitalSigns(
            @Valid @RequestBody CreateVitalSignsRequest request) {

        log.info("Creating standalone vital signs for patient: {}", request.getPatientProfileId());

        VitalSignsDTO vitalSigns = vitalSignsService.createVitalSigns(request);

        ApiResponse<VitalSignsDTO> response = ApiResponse.success(
                "Vital signs recorded successfully",
                vitalSigns,
                HttpStatus.CREATED.value()
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Get vital signs by ID
     */
    @GetMapping("/{vitalSignsId}")
    @PreAuthorize("hasAnyRole('PATIENT', 'DOCTOR', 'NURSE', 'LAB_TECHNICIAN', 'PHARMACIST', 'ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<VitalSignsDTO>> getVitalSignsById(
            @PathVariable Long vitalSignsId) {

        log.info("Retrieving vital signs: {}", vitalSignsId);

        VitalSignsDTO vitalSigns = vitalSignsService.getVitalSignsById(vitalSignsId);

        ApiResponse<VitalSignsDTO> response = ApiResponse.success(
                "Vital signs retrieved successfully",
                vitalSigns
        );

        return ResponseEntity.ok(response);
    }

    /**
     * Update vital signs
     */
    @PutMapping("/{vitalSignsId}")
    @PreAuthorize("hasAnyRole('DOCTOR', 'NURSE', 'LAB_TECHNICIAN', 'PHARMACIST', 'ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<VitalSignsDTO>> updateVitalSigns(
            @PathVariable Long vitalSignsId,
            @Valid @RequestBody VitalSignsDTO vitalSignsDTO) {

        log.info("Updating vital signs: {}", vitalSignsId);

        VitalSignsDTO updatedVitalSigns = vitalSignsService.updateVitalSigns(vitalSignsId, vitalSignsDTO);

        ApiResponse<VitalSignsDTO> response = ApiResponse.success(
                "Vital signs updated successfully",
                updatedVitalSigns
        );

        return ResponseEntity.ok(response);
    }

    /**
     * Delete vital signs
     */
    @DeleteMapping("/{vitalSignsId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<String>> deleteVitalSigns(
            @PathVariable Long vitalSignsId) {

        log.info("Deleting vital signs: {}", vitalSignsId);

        vitalSignsService.deleteVitalSigns(vitalSignsId);

        ApiResponse<String> response = ApiResponse.success("Vital signs deleted successfully");

        return ResponseEntity.ok(response);
    }

    /**
     * Get all vital signs for a patient (both standalone and appointment-linked)
     */
    @GetMapping("/patient/{patientProfileId}/history")
    @PreAuthorize("hasAnyRole('PATIENT', 'DOCTOR', 'NURSE', 'ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<List<VitalSignsDTO>>> getPatientVitalSignsHistory(
            @PathVariable Long patientProfileId) {

        log.info("Retrieving vital signs history for patient: {}", patientProfileId);

        List<VitalSignsDTO> vitalSignsHistory = vitalSignsService.getPatientVitalSignsHistory(patientProfileId);

        ApiResponse<List<VitalSignsDTO>> response = ApiResponse.success(
                "Patient vital signs history retrieved successfully",
                vitalSignsHistory
        );

        return ResponseEntity.ok(response);
    }

    /**
     * Get only standalone vital signs for a patient (not linked to appointments)
     */
    @GetMapping("/patient/{patientProfileId}/standalone")
    @PreAuthorize("hasAnyRole('PATIENT', 'DOCTOR', 'NURSE', 'ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<List<VitalSignsDTO>>> getPatientStandaloneVitalSigns(
            @PathVariable Long patientProfileId) {

        log.info("Retrieving standalone vital signs for patient: {}", patientProfileId);

        List<VitalSignsDTO> vitalSigns = vitalSignsService.getPatientStandaloneVitalSigns(patientProfileId);

        ApiResponse<List<VitalSignsDTO>> response = ApiResponse.success(
                "Standalone vital signs retrieved successfully",
                vitalSigns
        );

        return ResponseEntity.ok(response);
    }

    /**
     * Get patients with severe vital signs (for medical alerts)
     */
    @GetMapping("/severe")
    @PreAuthorize("hasAnyRole('DOCTOR', 'NURSE', 'ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<List<VitalSignsDTO>>> getPatientsWithSevereVitals() {

        log.info("Retrieving patients with severe vital signs");

        List<VitalSignsDTO> severeVitals = vitalSignsService.getPatientsWithSevereVitals();

        ApiResponse<List<VitalSignsDTO>> response = ApiResponse.success(
                "Severe vital signs retrieved successfully",
                severeVitals
        );

        return ResponseEntity.ok(response);
    }
}
