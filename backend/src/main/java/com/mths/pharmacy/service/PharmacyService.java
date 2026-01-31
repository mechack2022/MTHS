package com.mths.pharmacy.service;

import com.mths.pharmacy.dto.CreatePharmacyRequest;
import com.mths.pharmacy.dto.PharmacyResponse;
import com.mths.pharmacy.entity.Pharmacy;

import java.util.List;

/**
 * Service interface for managing pharmacies
 */
public interface PharmacyService {
    /**
     * Create a new pharmacy (Admin only - creates with PENDING_VERIFICATION status)
     */
    PharmacyResponse createPharmacy(CreatePharmacyRequest request);

    PharmacyResponse getPharmacyById(Long pharmacyId);
    PharmacyResponse getPharmacyByLicenseNumber(String licenseNumber);
    List<PharmacyResponse> getAllPharmacies();
    List<PharmacyResponse> getActivePharmacies();
    List<PharmacyResponse> getPharmaciesByStatus(Pharmacy.PharmacyStatus status);
    List<PharmacyResponse> getActivePharmaciesByCity(String city);
    List<PharmacyResponse> getActivePharmaciesByState(String state);
    PharmacyResponse updatePharmacy(Long pharmacyId, CreatePharmacyRequest request);
    PharmacyResponse verifyPharmacy(Long pharmacyId, String verifiedBy);
    PharmacyResponse activatePharmacy(Long pharmacyId);
    PharmacyResponse deactivatePharmacy(Long pharmacyId, String reason);
    void deletePharmacy(Long pharmacyId);
    PharmacyResponse updateRating(Long pharmacyId, double rating);
}
