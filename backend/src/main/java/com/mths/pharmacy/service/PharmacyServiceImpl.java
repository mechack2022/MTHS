package com.mths.pharmacy.service;

import com.mths.pharmacy.dto.CreatePharmacyRequest;
import com.mths.pharmacy.dto.PharmacyResponse;
import com.mths.pharmacy.entity.Pharmacy;
import com.mths.pharmacy.mapper.PharmacyMapper;
import com.mths.pharmacy.repository.PharmacyRepository;
import com.mths.shared.exceptions.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service implementation for managing pharmacies
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class PharmacyServiceImpl implements PharmacyService {

    private final PharmacyRepository pharmacyRepository;
    private final PharmacyMapper pharmacyMapper;

    @Override
    public PharmacyResponse createPharmacy(CreatePharmacyRequest request) {
        log.info("Admin creating pharmacy: {}", request.getPharmacyName());

        // Validate uniqueness
        if (pharmacyRepository.existsByLicenseNumber(request.getLicenseNumber())) {
            throw new IllegalArgumentException("Pharmacy with license number " + request.getLicenseNumber() + " already exists");
        }

        if (request.getRegistrationNumber() != null &&
            pharmacyRepository.existsByRegistrationNumber(request.getRegistrationNumber())) {
            throw new IllegalArgumentException("Pharmacy with registration number " + request.getRegistrationNumber() + " already exists");
        }

        if (pharmacyRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Pharmacy with email " + request.getEmail() + " already exists");
        }

        // Create pharmacy entity - automatically verified since admin is creating it
        Pharmacy pharmacy = buildPharmacyFromRequest(request);
        pharmacy.setStatus(Pharmacy.PharmacyStatus.VERIFIED);
        pharmacy.setIsActive(true);
        pharmacy.setVerificationDate(LocalDateTime.now());
        pharmacy.setVerifiedBy("ADMIN"); // Set by admin creating it

        Pharmacy savedPharmacy = pharmacyRepository.save(pharmacy);

        log.info("Pharmacy created and verified successfully: {} with ID: {} - Status: VERIFIED",
                savedPharmacy.getPharmacyName(), savedPharmacy.getId());

        return pharmacyMapper.toResponse(savedPharmacy);
    }

    @Override
    @Transactional(readOnly = true)
    public PharmacyResponse getPharmacyById(Long pharmacyId) {
        Pharmacy pharmacy = findPharmacyById(pharmacyId);
        return pharmacyMapper.toResponse(pharmacy);
    }

    @Override
    @Transactional(readOnly = true)
    public PharmacyResponse getPharmacyByLicenseNumber(String licenseNumber) {
        Pharmacy pharmacy = pharmacyRepository.findByLicenseNumber(licenseNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Pharmacy", "licenseNumber", licenseNumber));
        return pharmacyMapper.toResponse(pharmacy);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PharmacyResponse> getAllPharmacies() {
        log.info("Retrieving all pharmacies");
        List<Pharmacy> pharmacies = pharmacyRepository.findAll();
        return pharmacies.stream()
                .map(pharmacyMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<PharmacyResponse> getActivePharmacies() {
        log.info("Retrieving active pharmacies");
        List<Pharmacy> pharmacies = pharmacyRepository.findActivePharmacies();
        return pharmacies.stream()
                .map(pharmacyMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<PharmacyResponse> getPharmaciesByStatus(Pharmacy.PharmacyStatus status) {
        log.info("Retrieving pharmacies with status: {}", status);
        List<Pharmacy> pharmacies = pharmacyRepository.findByStatus(status);
        return pharmacies.stream()
                .map(pharmacyMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<PharmacyResponse> getActivePharmaciesByCity(String city) {
        log.info("Retrieving active pharmacies in city: {}", city);
        List<Pharmacy> pharmacies = pharmacyRepository.findActivePharmaciesByCity(city);
        return pharmacies.stream()
                .map(pharmacyMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<PharmacyResponse> getActivePharmaciesByState(String state) {
        log.info("Retrieving active pharmacies in state: {}", state);
        List<Pharmacy> pharmacies = pharmacyRepository.findActivePharmaciesByState(state);
        return pharmacies.stream()
                .map(pharmacyMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public PharmacyResponse updatePharmacy(Long pharmacyId, CreatePharmacyRequest request) {
        log.info("Updating pharmacy: {}", pharmacyId);

        Pharmacy pharmacy = findPharmacyById(pharmacyId);

        // Update fields
        pharmacy.setPharmacyName(request.getPharmacyName());
        pharmacy.setEmail(request.getEmail());
        pharmacy.setPhoneNumber(request.getPhoneNumber());
        pharmacy.setAlternativePhone(request.getAlternativePhone());
        pharmacy.setAddress(request.getAddress());
        pharmacy.setCity(request.getCity());
        pharmacy.setState(request.getState());
        pharmacy.setPostalCode(request.getPostalCode());
        pharmacy.setCountry(request.getCountry());
        pharmacy.setLatitude(request.getLatitude());
        pharmacy.setLongitude(request.getLongitude());
        pharmacy.setOpeningTime(request.getOpeningTime());
        pharmacy.setClosingTime(request.getClosingTime());
        pharmacy.setIs24Hours(request.getIs24Hours());
        pharmacy.setOperatingDays(request.getOperatingDays());
        pharmacy.setOwnerName(request.getOwnerName());
        pharmacy.setSuperintendentPharmacist(request.getSuperintendentPharmacist());
        pharmacy.setServicesOffered(request.getServicesOffered());
        pharmacy.setAcceptsInsurance(request.getAcceptsInsurance());
        pharmacy.setInsuranceProviders(request.getInsuranceProviders());
        pharmacy.setPublicNotes(request.getPublicNotes());

        Pharmacy updatedPharmacy = pharmacyRepository.save(pharmacy);

        log.info("Pharmacy updated successfully: {}", pharmacyId);

        return pharmacyMapper.toResponse(updatedPharmacy);
    }

    @Override
    public PharmacyResponse verifyPharmacy(Long pharmacyId, String verifiedBy) {
        log.info("Verifying pharmacy: {} by {}", pharmacyId, verifiedBy);

        Pharmacy pharmacy = findPharmacyById(pharmacyId);

        pharmacy.setStatus(Pharmacy.PharmacyStatus.VERIFIED);
        pharmacy.setVerificationDate(LocalDateTime.now());
        pharmacy.setVerifiedBy(verifiedBy);
        pharmacy.setIsActive(true);

        Pharmacy verifiedPharmacy = pharmacyRepository.save(pharmacy);

        log.info("Pharmacy verified successfully: {}", pharmacyId);

        return pharmacyMapper.toResponse(verifiedPharmacy);
    }

    @Override
    public PharmacyResponse activatePharmacy(Long pharmacyId) {
        log.info("Activating pharmacy: {}", pharmacyId);

        Pharmacy pharmacy = findPharmacyById(pharmacyId);

        if (pharmacy.getStatus() != Pharmacy.PharmacyStatus.VERIFIED) {
            throw new IllegalStateException("Only verified pharmacies can be activated");
        }

        pharmacy.setIsActive(true);
        pharmacy.setDeactivationReason(null);

        Pharmacy activatedPharmacy = pharmacyRepository.save(pharmacy);

        log.info("Pharmacy activated successfully: {}", pharmacyId);

        return pharmacyMapper.toResponse(activatedPharmacy);
    }

    @Override
    public PharmacyResponse deactivatePharmacy(Long pharmacyId, String reason) {
        log.info("Deactivating pharmacy: {} with reason: {}", pharmacyId, reason);

        Pharmacy pharmacy = findPharmacyById(pharmacyId);

        pharmacy.setIsActive(false);
        pharmacy.setDeactivationReason(reason);

        Pharmacy deactivatedPharmacy = pharmacyRepository.save(pharmacy);

        log.info("Pharmacy deactivated successfully: {}", pharmacyId);

        return pharmacyMapper.toResponse(deactivatedPharmacy);
    }

    @Override
    public void deletePharmacy(Long pharmacyId) {
        log.info("Deleting pharmacy: {}", pharmacyId);

        Pharmacy pharmacy = findPharmacyById(pharmacyId);
        pharmacyRepository.delete(pharmacy);

        log.info("Pharmacy deleted successfully: {}", pharmacyId);
    }

    @Override
    public PharmacyResponse updateRating(Long pharmacyId, double rating) {
        log.info("Updating rating for pharmacy: {} with rating: {}", pharmacyId, rating);

        if (rating < 0 || rating > 5) {
            throw new IllegalArgumentException("Rating must be between 0 and 5");
        }

        Pharmacy pharmacy = findPharmacyById(pharmacyId);
        pharmacy.updateRating(rating);

        Pharmacy updatedPharmacy = pharmacyRepository.save(pharmacy);

        log.info("Pharmacy rating updated successfully: {}", pharmacyId);

        return pharmacyMapper.toResponse(updatedPharmacy);
    }

    // ========================================================================
    // PRIVATE HELPER METHODS
    // ========================================================================

    private Pharmacy findPharmacyById(Long pharmacyId) {
        return pharmacyRepository.findById(pharmacyId)
                .orElseThrow(() -> new ResourceNotFoundException("Pharmacy", "id", pharmacyId));
    }

    private Pharmacy buildPharmacyFromRequest(CreatePharmacyRequest request) {
        Pharmacy pharmacy = new Pharmacy();
        pharmacy.setPharmacyName(request.getPharmacyName());
        pharmacy.setLicenseNumber(request.getLicenseNumber());
        pharmacy.setRegistrationNumber(request.getRegistrationNumber());
        pharmacy.setEmail(request.getEmail());
        pharmacy.setPhoneNumber(request.getPhoneNumber());
        pharmacy.setAlternativePhone(request.getAlternativePhone());
        pharmacy.setAddress(request.getAddress());
        pharmacy.setCity(request.getCity());
        pharmacy.setState(request.getState());
        pharmacy.setPostalCode(request.getPostalCode());
        pharmacy.setCountry(request.getCountry() != null ? request.getCountry() : "Nigeria");
        pharmacy.setLatitude(request.getLatitude());
        pharmacy.setLongitude(request.getLongitude());
        pharmacy.setOpeningTime(request.getOpeningTime());
        pharmacy.setClosingTime(request.getClosingTime());
        pharmacy.setIs24Hours(request.getIs24Hours() != null ? request.getIs24Hours() : false);
        pharmacy.setOperatingDays(request.getOperatingDays());
        pharmacy.setOwnerName(request.getOwnerName());
        pharmacy.setSuperintendentPharmacist(request.getSuperintendentPharmacist());
        pharmacy.setServicesOffered(request.getServicesOffered());
        pharmacy.setAcceptsInsurance(request.getAcceptsInsurance() != null ? request.getAcceptsInsurance() : false);
        pharmacy.setInsuranceProviders(request.getInsuranceProviders());
        pharmacy.setPublicNotes(request.getPublicNotes());
        return pharmacy;
    }
}
