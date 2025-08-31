package com.auth.service.service;

import com.auth.service.entity.DoctorProfile;
import com.auth.service.entity.PatientProfile;
import com.auth.service.entity.User;
import com.auth.service.entity.UserProfile;
import com.auth.service.exceptions.BadRequestException;
import com.auth.service.exceptions.ResourceNotFoundException;
import com.auth.service.repository.DoctorProfileRepository;
import com.auth.service.repository.PatientProfileRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Service for validating user accounts and profiles before allowing operations
 * This service can be reused across multiple services that need account validation
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AccountValidationService {

    private final PatientProfileRepository patientProfileRepository;
    private final DoctorProfileRepository doctorProfileRepository;

    /**
     * Validates a patient account for appointment creation or other operations
     * @param patientProfileId The patient profile ID to validate
     * @throws BadRequestException if validation fails
     * @throws ResourceNotFoundException if profile doesn't exist
     */
    public void validatePatientAccount(Long patientProfileId) {
        log.debug("Validating patient account for profile ID: {}", patientProfileId);
        
        PatientProfile profile = getPatientProfile(patientProfileId);
        User user = profile.getUser();
        
        validateUserAccount(user, "Patient");
        validateProfileCompletion(profile, "Patient");
        
        log.debug("Patient account validation passed for ID: {}", patientProfileId);
    }

    /**
     * Validates a doctor account for appointment creation or other operations
     * @param doctorProfileId The doctor profile ID to validate
     * @throws BadRequestException if validation fails
     * @throws ResourceNotFoundException if profile doesn't exist
     */
    public void validateDoctorAccount(Long doctorProfileId) {
        log.debug("Validating doctor account for profile ID: {}", doctorProfileId);
        
        DoctorProfile profile = getDoctorProfile(doctorProfileId);
        User user = profile.getUser();
        
        validateUserAccount(user, "Doctor");
        validateProfileCompletion(profile, "Doctor");
        validateDoctorSpecificRequirements(profile);
        
        log.debug("Doctor account validation passed for ID: {}", doctorProfileId);
    }

    /**
     * Validates any user account without specific profile requirements
     * @param user The user to validate
     * @param userType The type of user (for error messages)
     */
    public void validateUserAccount(User user, String userType) {
        if (user == null) {
            throw new BadRequestException("account", userType + " account not found.");
        }
        
        if (!user.getIsActive()) {
            throw new BadRequestException("account", userType + " account is deactivated. Please contact support.");
        }
        
        if (!user.getAccountVerified()) {
            throw new BadRequestException("verification", userType + " account is not verified. Please verify your account first.");
        }
        
        if (!user.getMailVerified()) {
            throw new BadRequestException("verification", userType + " email is not verified. Please verify your email first.");
        }
    }

    /**
     * Validates that a profile is complete
     * @param profile The profile to validate
     * @param userType The type of user (for error messages)
     */
    public void validateProfileCompletion(UserProfile profile, String userType) {
        if (!profile.isProfileComplete()) {
            throw new BadRequestException("profile", userType + " profile is incomplete. Please complete your profile first.");
        }
    }

    /**
     * Validates doctor-specific requirements
     * @param doctorProfile The doctor profile to validate
     */
    public void validateDoctorSpecificRequirements(DoctorProfile doctorProfile) {
        if (!doctorProfile.getAvailableForConsultation()) {
            throw new BadRequestException("availability", "Doctor is not currently available for consultations.");
        }
        
        if (!doctorProfile.isLicenseValid()) {
            throw new BadRequestException("license", "Doctor's medical license is invalid.");
        }
    }

    /**
     * Checks if a patient account is valid without throwing exceptions
     * @param patientProfileId The patient profile ID
     * @return true if valid, false otherwise
     */
    public boolean isPatientAccountValid(Long patientProfileId) {
        try {
            validatePatientAccount(patientProfileId);
            return true;
        } catch (Exception e) {
            log.debug("Patient account validation failed for ID: {} - {}", patientProfileId, e.getMessage());
            return false;
        }
    }

    /**
     * Checks if a doctor account is valid without throwing exceptions
     * @param doctorProfileId The doctor profile ID
     * @return true if valid, false otherwise
     */
    public boolean isDoctorAccountValid(Long doctorProfileId) {
        try {
            validateDoctorAccount(doctorProfileId);
            return true;
        } catch (Exception e) {
            log.debug("Doctor account validation failed for ID: {} - {}", doctorProfileId, e.getMessage());
            return false;
        }
    }

    /**
     * Gets a patient profile with user data
     * @param patientProfileId The patient profile ID
     * @return The patient profile with user
     */
    public PatientProfile getPatientProfile(Long patientProfileId) {
        return patientProfileRepository.findByIdWithUser(patientProfileId)
                .orElseThrow(() -> new ResourceNotFoundException("PatientProfile", "id", patientProfileId));
    }

    /**
     * Gets a doctor profile with user data
     * @param doctorProfileId The doctor profile ID
     * @return The doctor profile with user
     */
    public DoctorProfile getDoctorProfile(Long doctorProfileId) {
        return doctorProfileRepository.findByIdWithUser(doctorProfileId)
                .orElseThrow(() -> new ResourceNotFoundException("DoctorProfile", "id", doctorProfileId));
    }
}