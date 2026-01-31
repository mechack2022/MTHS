package com.mths.pharmacy.controller;

import com.mths.pharmacy.dto.CreatePharmacyRequest;
import com.mths.pharmacy.dto.PharmacyResponse;
import com.mths.pharmacy.entity.Pharmacy;
import com.mths.pharmacy.service.PharmacyService;
import com.mths.shared.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller for managing pharmacies
 */
@RestController
@RequestMapping("/api/v1/pharmacies")
@Slf4j
@RequiredArgsConstructor
@Tag(name = "Pharmacy Management", description = "APIs for managing pharmacies")
public class PharmacyController {

    private final PharmacyService pharmacyService;

    /**
     * Create a new pharmacy
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    @Operation(summary = "Create pharmacy", description = "Register a new pharmacy in the system")
    public ResponseEntity<ApiResponse<PharmacyResponse>> createPharmacy(
            @Valid @RequestBody CreatePharmacyRequest request) {

        log.info("Creating pharmacy: {}", request.getPharmacyName());

        PharmacyResponse pharmacy = pharmacyService.createPharmacy(request);

        ApiResponse<PharmacyResponse> response = ApiResponse.success(
                "Pharmacy created successfully",
                pharmacy,
                HttpStatus.CREATED.value()
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Get pharmacy by ID
     */
    @GetMapping("/{pharmacyId}")
    @PreAuthorize("hasAnyRole('PATIENT', 'DOCTOR', 'PHARMACIST', 'ADMIN', 'SUPER_ADMIN')")
    @Operation(summary = "Get pharmacy by ID", description = "Retrieve pharmacy details by ID")
    public ResponseEntity<ApiResponse<PharmacyResponse>> getPharmacyById(
            @Parameter(description = "Pharmacy ID") @PathVariable Long pharmacyId) {

        log.info("Retrieving pharmacy: {}", pharmacyId);

        PharmacyResponse pharmacy = pharmacyService.getPharmacyById(pharmacyId);

        ApiResponse<PharmacyResponse> response = ApiResponse.success(
                "Pharmacy retrieved successfully",
                pharmacy
        );

        return ResponseEntity.ok(response);
    }

    /**
     * Get pharmacy by license number
     */
    @GetMapping("/license/{licenseNumber}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    @Operation(summary = "Get pharmacy by license number", description = "Retrieve pharmacy by license number")
    public ResponseEntity<ApiResponse<PharmacyResponse>> getPharmacyByLicenseNumber(
            @Parameter(description = "License number") @PathVariable String licenseNumber) {

        log.info("Retrieving pharmacy by license number: {}", licenseNumber);

        PharmacyResponse pharmacy = pharmacyService.getPharmacyByLicenseNumber(licenseNumber);

        ApiResponse<PharmacyResponse> response = ApiResponse.success(
                "Pharmacy retrieved successfully",
                pharmacy
        );

        return ResponseEntity.ok(response);
    }

    /**
     * Get all pharmacies
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    @Operation(summary = "Get all pharmacies", description = "Get all pharmacies in the system")
    public ResponseEntity<ApiResponse<List<PharmacyResponse>>> getAllPharmacies() {

        log.info("Retrieving all pharmacies");

        List<PharmacyResponse> pharmacies = pharmacyService.getAllPharmacies();

        ApiResponse<List<PharmacyResponse>> response = ApiResponse.success(
                "Pharmacies retrieved successfully",
                pharmacies
        );

        return ResponseEntity.ok(response);
    }

    /**
     * Get active pharmacies
     */
    @GetMapping("/active")
    @PreAuthorize("hasAnyRole('PATIENT', 'DOCTOR', 'PHARMACIST', 'ADMIN', 'SUPER_ADMIN')")
    @Operation(summary = "Get active pharmacies", description = "Get all active and verified pharmacies")
    public ResponseEntity<ApiResponse<List<PharmacyResponse>>> getActivePharmacies() {

        log.info("Retrieving active pharmacies");

        List<PharmacyResponse> pharmacies = pharmacyService.getActivePharmacies();

        ApiResponse<List<PharmacyResponse>> response = ApiResponse.success(
                "Active pharmacies retrieved successfully",
                pharmacies
        );

        return ResponseEntity.ok(response);
    }

    /**
     * Get pharmacies by status
     */
    @GetMapping("/status/{status}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    @Operation(summary = "Get pharmacies by status", description = "Get pharmacies filtered by status")
    public ResponseEntity<ApiResponse<List<PharmacyResponse>>> getPharmaciesByStatus(
            @Parameter(description = "Pharmacy status") @PathVariable Pharmacy.PharmacyStatus status) {

        log.info("Retrieving pharmacies with status: {}", status);

        List<PharmacyResponse> pharmacies = pharmacyService.getPharmaciesByStatus(status);

        ApiResponse<List<PharmacyResponse>> response = ApiResponse.success(
                "Pharmacies by status retrieved successfully",
                pharmacies
        );

        return ResponseEntity.ok(response);
    }

    /**
     * Get active pharmacies by city
     */
    @GetMapping("/city/{city}")
    @PreAuthorize("hasAnyRole('PATIENT', 'DOCTOR', 'PHARMACIST', 'ADMIN', 'SUPER_ADMIN')")
    @Operation(summary = "Get pharmacies by city", description = "Get active pharmacies in a specific city")
    public ResponseEntity<ApiResponse<List<PharmacyResponse>>> getActivePharmaciesByCity(
            @Parameter(description = "City name") @PathVariable String city) {

        log.info("Retrieving active pharmacies in city: {}", city);

        List<PharmacyResponse> pharmacies = pharmacyService.getActivePharmaciesByCity(city);

        ApiResponse<List<PharmacyResponse>> response = ApiResponse.success(
                "Pharmacies by city retrieved successfully",
                pharmacies
        );

        return ResponseEntity.ok(response);
    }

    /**
     * Get active pharmacies by state
     */
    @GetMapping("/state/{state}")
    @PreAuthorize("hasAnyRole('PATIENT', 'DOCTOR', 'PHARMACIST', 'ADMIN', 'SUPER_ADMIN')")
    @Operation(summary = "Get pharmacies by state", description = "Get active pharmacies in a specific state")
    public ResponseEntity<ApiResponse<List<PharmacyResponse>>> getActivePharmaciesByState(
            @Parameter(description = "State name") @PathVariable String state) {

        log.info("Retrieving active pharmacies in state: {}", state);

        List<PharmacyResponse> pharmacies = pharmacyService.getActivePharmaciesByState(state);

        ApiResponse<List<PharmacyResponse>> response = ApiResponse.success(
                "Pharmacies by state retrieved successfully",
                pharmacies
        );

        return ResponseEntity.ok(response);
    }

    /**
     * Update pharmacy details
     */
    @PutMapping("/{pharmacyId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    @Operation(summary = "Update pharmacy", description = "Update pharmacy details")
    public ResponseEntity<ApiResponse<PharmacyResponse>> updatePharmacy(
            @Parameter(description = "Pharmacy ID") @PathVariable Long pharmacyId,
            @Valid @RequestBody CreatePharmacyRequest request) {

        log.info("Updating pharmacy: {}", pharmacyId);

        PharmacyResponse pharmacy = pharmacyService.updatePharmacy(pharmacyId, request);

        ApiResponse<PharmacyResponse> response = ApiResponse.success(
                "Pharmacy updated successfully",
                pharmacy
        );

        return ResponseEntity.ok(response);
    }

    /**
     * Verify pharmacy
     */
    @PutMapping("/{pharmacyId}/verify")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    @Operation(summary = "Verify pharmacy", description = "Verify a pharmacy registration")
    public ResponseEntity<ApiResponse<PharmacyResponse>> verifyPharmacy(
            @Parameter(description = "Pharmacy ID") @PathVariable Long pharmacyId,
            @Parameter(description = "Verifier name") @RequestParam String verifiedBy) {

        log.info("Verifying pharmacy: {}", pharmacyId);

        PharmacyResponse pharmacy = pharmacyService.verifyPharmacy(pharmacyId, verifiedBy);

        ApiResponse<PharmacyResponse> response = ApiResponse.success(
                "Pharmacy verified successfully",
                pharmacy
        );

        return ResponseEntity.ok(response);
    }

    /**
     * Activate pharmacy
     */
    @PutMapping("/{pharmacyId}/activate")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    @Operation(summary = "Activate pharmacy", description = "Activate a pharmacy")
    public ResponseEntity<ApiResponse<PharmacyResponse>> activatePharmacy(
            @Parameter(description = "Pharmacy ID") @PathVariable Long pharmacyId) {

        log.info("Activating pharmacy: {}", pharmacyId);

        PharmacyResponse pharmacy = pharmacyService.activatePharmacy(pharmacyId);

        ApiResponse<PharmacyResponse> response = ApiResponse.success(
                "Pharmacy activated successfully",
                pharmacy
        );

        return ResponseEntity.ok(response);
    }

    /**
     * Deactivate pharmacy
     */
    @PutMapping("/{pharmacyId}/deactivate")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    @Operation(summary = "Deactivate pharmacy", description = "Deactivate a pharmacy")
    public ResponseEntity<ApiResponse<PharmacyResponse>> deactivatePharmacy(
            @Parameter(description = "Pharmacy ID") @PathVariable Long pharmacyId,
            @Parameter(description = "Deactivation reason") @RequestParam String reason) {

        log.info("Deactivating pharmacy: {}", pharmacyId);

        PharmacyResponse pharmacy = pharmacyService.deactivatePharmacy(pharmacyId, reason);

        ApiResponse<PharmacyResponse> response = ApiResponse.success(
                "Pharmacy deactivated successfully",
                pharmacy
        );

        return ResponseEntity.ok(response);
    }

    /**
     * Delete pharmacy
     */
    @DeleteMapping("/{pharmacyId}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN')")
    @Operation(summary = "Delete pharmacy", description = "Delete a pharmacy from the system")
    public ResponseEntity<ApiResponse<String>> deletePharmacy(
            @Parameter(description = "Pharmacy ID") @PathVariable Long pharmacyId) {

        log.info("Deleting pharmacy: {}", pharmacyId);

        pharmacyService.deletePharmacy(pharmacyId);

        ApiResponse<String> response = ApiResponse.success("Pharmacy deleted successfully");

        return ResponseEntity.ok(response);
    }

    /**
     * Update pharmacy rating
     */
    @PutMapping("/{pharmacyId}/rating")
    @PreAuthorize("hasAnyRole('PATIENT', 'ADMIN', 'SUPER_ADMIN')")
    @Operation(summary = "Update pharmacy rating", description = "Add a new rating for a pharmacy")
    public ResponseEntity<ApiResponse<PharmacyResponse>> updateRating(
            @Parameter(description = "Pharmacy ID") @PathVariable Long pharmacyId,
            @Parameter(description = "Rating (0-5)") @RequestParam double rating) {

        log.info("Updating rating for pharmacy: {} with rating: {}", pharmacyId, rating);

        PharmacyResponse pharmacy = pharmacyService.updateRating(pharmacyId, rating);

        ApiResponse<PharmacyResponse> response = ApiResponse.success(
                "Pharmacy rating updated successfully",
                pharmacy
        );

        return ResponseEntity.ok(response);
    }
}
