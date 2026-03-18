package com.mths.pharmacy.mapper;

import com.mths.pharmacy.dto.PharmacyResponse;
import com.mths.pharmacy.entity.Pharmacy;
import org.springframework.stereotype.Component;

/**
 * Mapper for converting Pharmacy entities to DTOs
 */
@Component
public class PharmacyMapper {

    public PharmacyResponse toResponse(Pharmacy pharmacy) {
        if (pharmacy == null) {
            return null;
        }

        PharmacyResponse response = new PharmacyResponse();
        response.setId(pharmacy.getId());
        response.setPharmacyName(pharmacy.getPharmacyName());
        response.setLicenseNumber(pharmacy.getLicenseNumber());
        response.setRegistrationNumber(pharmacy.getRegistrationNumber());
        response.setEmail(pharmacy.getEmail());
        response.setPhoneNumber(pharmacy.getPhoneNumber());
        response.setAlternativePhone(pharmacy.getAlternativePhone());
        response.setAddress(pharmacy.getAddress());
        response.setCity(pharmacy.getCity());
        response.setState(pharmacy.getState());
        response.setPostalCode(pharmacy.getPostalCode());
        response.setCountry(pharmacy.getCountry());
        response.setFullAddress(pharmacy.getFullAddress());
        response.setLatitude(pharmacy.getLatitude());
        response.setLongitude(pharmacy.getLongitude());
        response.setOpeningTime(pharmacy.getOpeningTime());
        response.setClosingTime(pharmacy.getClosingTime());
        response.setIs24Hours(pharmacy.getIs24Hours());
        response.setOperatingDays(pharmacy.getOperatingDays());
        response.setOwnerName(pharmacy.getOwnerName());
        response.setSuperintendentPharmacist(pharmacy.getSuperintendentPharmacist());
        response.setServicesOffered(pharmacy.getServicesOffered());
        response.setAcceptsInsurance(pharmacy.getAcceptsInsurance());
        response.setInsuranceProviders(pharmacy.getInsuranceProviders());
        response.setStatus(pharmacy.getStatus());
        response.setVerificationDate(pharmacy.getVerificationDate());
        response.setVerifiedBy(pharmacy.getVerifiedBy());
        response.setIsActive(pharmacy.getIsActive());
        response.setDeactivationReason(pharmacy.getDeactivationReason());
        response.setRating(pharmacy.getRating());
        response.setTotalReviews(pharmacy.getTotalReviews());
        response.setTotalPrescriptionsFilled(pharmacy.getTotalPrescriptionsFilled());
        response.setPublicNotes(pharmacy.getPublicNotes());

        // Helper/computed fields
        response.setIsVerified(pharmacy.isVerified());
        response.setCanAcceptPrescriptions(pharmacy.canAcceptPrescriptions());
        response.setIsCurrentlyOpen(pharmacy.isCurrentlyOpen());

        // Audit fields
        response.setCreatedAt(pharmacy.getCreatedAt());
        response.setUpdatedAt(pharmacy.getUpdatedAt());

        return response;
    }
}
