package com.auth.service.service;

import com.auth.service.dto.*;
import com.auth.service.entity.*;
import com.auth.service.exceptions.BadRequestException;
import com.auth.service.exceptions.ResourceNotFoundException;
import com.auth.service.mapper.ProfileMapper;
import com.auth.service.mapper.LabTechnicianMapper;
import com.auth.service.repository.UserRepository;
import com.auth.service.utils.ProfileCompletionStatus;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
public class UserProfileServiceImpl implements UserProfileService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RolePermissionService rolePermissionService;

    @Autowired
    private ProfileMapper profileMapper;

    @Autowired
    private LabTechnicianMapper labTechnicianMapper;

    // ========================================================================
    // PROFILE CREATION METHODS
    // ========================================================================


    @Override
    @PreAuthorize("@userProfileService.canUserAccessProfileService(#userId)")
    public PatientProfileDTO createPatientProfile(String userId, CreatePatientProfileRequest request) {
        User user = findUserByUuid(userId);

        // Validate user can have a patient profile
        validateUserForProfileCreation(user, UserProfile.ProfileType.PATIENT);

        // Check if patient profile already exists
        if (user.hasPatientProfile()) {
            throw new BadRequestException("profile", "Patient profile already exists for this user");
        }

        // Validate unique fields
        validateUniquePatientFields(request);

        // Create patient profile
        PatientProfile patientProfile = new PatientProfile();
        patientProfile.setDateOfBirth(request.getDateOfBirth());
        patientProfile.setGender(request.getGender());
        patientProfile.setNIN(request.getNIN());
        patientProfile.setMaritalStatus(request.getMaritalStatus());
        patientProfile.setBloodType(request.getBloodType());
        patientProfile.setPreferredHospital(request.getPreferredHospital());
        patientProfile.setEmergencyContactName(request.getEmergencyContactName());
        patientProfile.setEmergencyContactPhone(request.getEmergencyContactPhone());

        // Set common profile fields
        patientProfile.setPhoneNumber(request.getPhoneNumber());
        patientProfile.setAddress(request.getAddress());
        patientProfile.setProfileImageUrl(request.getProfileImageUrl());

        // Add profile to user
        user.addProfile(patientProfile);

        // Save user with profile
        User savedUser = userRepository.save(user);

        // Check if profile is complete and upgrade role if necessary
        checkAndUpgradeUserRole(savedUser);

        return profileMapper.toPatientProfileDTO(savedUser.getPatientProfile());
    }

    @Override
    @PreAuthorize("@userProfileService.canUserAccessProfileService(#userId)")
    public DoctorProfileDTO createDoctorProfile(String userId, CreateDoctorProfileRequest request) {
        User user = findUserByUuid(userId);

        // Validate user can have a doctor profile
        validateUserForProfileCreation(user, UserProfile.ProfileType.DOCTOR);

        // Check if doctor profile already exists
        if (user.hasDoctorProfile()) {
            throw new BadRequestException("profile", "Doctor profile already exists for this user");
        }

        // Validate unique fields
        validateUniqueDoctorFields(request);

        // Create doctor profile
        DoctorProfile doctorProfile = new DoctorProfile();
        doctorProfile.setMedicalLicenseNumber(request.getMedicalLicenseNumber());
        doctorProfile.setSpecialization(request.getSpecialization());
        doctorProfile.setYearsOfExperience(request.getYearsOfExperience());
        doctorProfile.setHospitalAffiliation(request.getHospitalAffiliation());
        doctorProfile.setConsultationFee(request.getConsultationFee());
        doctorProfile.setAvailableForConsultation(
                request.getAvailableForConsultation() != null ?
                        request.getAvailableForConsultation() : true
        );
        doctorProfile.setOfficeHours(request.getOfficeHours());
        doctorProfile.setMedicalSchool(request.getMedicalSchool());
        doctorProfile.setBoardCertifications(request.getBoardCertifications());

        // Set common profile fields
        doctorProfile.setPhoneNumber(request.getPhoneNumber());
        doctorProfile.setAddress(request.getAddress());
        doctorProfile.setProfileImageUrl(request.getProfileImageUrl());

        // Add profile to user
        user.addProfile(doctorProfile);

        // Save user with profile
        User savedUser = userRepository.save(user);

        // Check if profile is complete and upgrade role if necessary
        checkAndUpgradeUserRole(savedUser);

        return profileMapper.toDoctorProfileDTO(savedUser.getDoctorProfile());
    }

    @Override
    @PreAuthorize("@userProfileService.canUserAccessProfileService(#userId)")
    public LabTechnicianProfileDTO createLabTechnicianProfile(String userId, CreateLabTechnicianProfileRequest request) {
        User user = findUserByUuid(userId);

        // Validate user can have a lab technician profile
        validateUserForProfileCreation(user, UserProfile.ProfileType.LAB_TECHNICIAN);

        // Validate unique fields
        validateUniqueLabTechnicianFields(request);

        // Create lab technician profile
        LabTechnicianProfile labTechProfile = labTechnicianMapper.toEntity(request);

        // Add profile to user
        user.addProfile(labTechProfile);

        // Save user with profile
        User savedUser = userRepository.save(user);

        // Check if profile is complete and upgrade role if necessary
        checkAndUpgradeUserRole(savedUser);

        return labTechnicianMapper.toDTO(savedUser.getLabTechnicianProfile());
    }

    // ========================================================================
    // PROFILE UPDATE METHODS
    // ========================================================================

    @Override
    @PreAuthorize("@userProfileService.canUserAccessProfileService(#userId)")
    public PatientProfileDTO updatePatientProfile(String userId, UpdatePatientProfileRequest request) {
        User user = findUserByUuid(userId);
        PatientProfile profile = user.getPatientProfile();

        if (profile == null) {
            throw new ResourceNotFoundException("Profile", "Patient profile not found for user", userId);
        }

        // Validate unique fields if they're being changed
        validateUniquePatientFieldsForUpdate(profile, request);

        // Update profile fields
        updatePatientProfileFields(profile, request);

        // Save updated profile
        User savedUser = userRepository.save(user);

        // Check if profile completion status changed
        checkAndUpgradeUserRole(savedUser);

        return profileMapper.toPatientProfileDTO(savedUser.getPatientProfile());
    }

    @Override
    @PreAuthorize("@userProfileService.canUserAccessProfileService(#userId)")
    public DoctorProfileDTO updateDoctorProfile(String userId, UpdateDoctorProfileRequest request) {
        User user = findUserByUuid(userId);
        DoctorProfile profile = user.getDoctorProfile();

        if (profile == null) {
            throw new ResourceNotFoundException("Profile", "Doctor profile not found for user", userId);
        }

        // Validate unique fields if they're being changed
        validateUniqueDoctorFieldsForUpdate(profile, request);

        // Update profile fields
        updateDoctorProfileFields(profile, request);

        // Save updated profile
        User savedUser = userRepository.save(user);

        // Check if profile completion status changed
        checkAndUpgradeUserRole(savedUser);

        return profileMapper.toDoctorProfileDTO(savedUser.getDoctorProfile());
    }

    @Override
    @PreAuthorize("@userProfileService.canUserAccessProfileService(#userId)")
    public LabTechnicianProfileDTO updateLabTechnicianProfile(String userId, UpdateLabTechnicianProfileRequest request) {
        User user = findUserByUuid(userId);
        LabTechnicianProfile profile = user.getLabTechnicianProfile();

        if (profile == null) {
            throw new ResourceNotFoundException("Profile", "Lab technician profile not found for user", userId);
        }

        // Validate unique fields if they're being changed
        validateUniqueLabTechnicianFieldsForUpdate(profile, request);

        // Update profile fields using MapStruct
        labTechnicianMapper.updateEntityFromRequest(request, profile);

        // Save updated profile
        User savedUser = userRepository.save(user);

        // Check if profile completion status changed
        checkAndUpgradeUserRole(savedUser);

        return labTechnicianMapper.toDTO(savedUser.getLabTechnicianProfile());
    }

    // ========================================================================
    // PROFILE RETRIEVAL METHODS
    // ========================================================================

    @Override
    @PreAuthorize("@userProfileService.canUserAccessProfileService(#userId)")
    public UserProfileDTO getUserProfile(String userId) {
        User user = findUserByUuid(userId);

        UserProfileDTO profileDTO = new UserProfileDTO();
        profileDTO.setUserId(userId);
        profileDTO.setEmail(user.getEmail());
        profileDTO.setFullName(user.getFullName());
        profileDTO.setAccountType(user.getAccountType());
        profileDTO.setHasPrimaryProfile(user.hasPrimaryProfile());

        if (user.hasPatientProfile()) {
            profileDTO.setPatientProfile(profileMapper.toPatientProfileDTO(user.getPatientProfile()));
        }

        if (user.hasDoctorProfile()) {
            profileDTO.setDoctorProfile(profileMapper.toDoctorProfileDTO(user.getDoctorProfile()));
        }

        // Add profile completion status
        UserProfile primaryProfile = user.getPrimaryProfile();
        if (primaryProfile != null) {
            profileDTO.setProfileComplete(primaryProfile.isProfileComplete());
            profileDTO.setMissingFields(getMissingRequiredFields(primaryProfile));
        } else {
            profileDTO.setProfileComplete(false);
            profileDTO.setMissingFields(List.of("No profile created"));
        }

        return profileDTO;
    }

    @Override
    @PreAuthorize("@userProfileService.canUserAccessProfileService(#userId)")
    public PatientProfileDTO getPatientProfile(String userId) {
        User user = findUserByUuid(userId);
        PatientProfile profile = user.getPatientProfile();

        if (profile == null) {
            throw new ResourceNotFoundException("Profile", "Patient profile not found for user", userId);
        }

        return profileMapper.toPatientProfileDTO(profile);
    }

    @Override
    @PreAuthorize("@userProfileService.canUserAccessProfileService(#userId)")
    public DoctorProfileDTO getDoctorProfile(String userId) {
        User user = findUserByUuid(userId);
        DoctorProfile profile = user.getDoctorProfile();

        if (profile == null) {
            throw new ResourceNotFoundException("Profile", "Doctor profile not found for user", userId);
        }

        return profileMapper.toDoctorProfileDTO(profile);
    }

    @Override
    @PreAuthorize("@userProfileService.canUserAccessProfileService(#userId)")
    public LabTechnicianProfileDTO getLabTechnicianProfile(String userId) {
        User user = findUserByUuid(userId);
        LabTechnicianProfile profile = user.getLabTechnicianProfile();

        if (profile == null) {
            throw new ResourceNotFoundException("Profile", "Lab technician profile not found for user", userId);
        }

        return labTechnicianMapper.toDTO(profile);
    }

    // ========================================================================
    // PROFILE STATUS METHODS
    // ========================================================================

    @Override
    public boolean hasProfile(String userId) {
        User user = findUserByUuid(userId);
        return user.hasPrimaryProfile();
    }

    @Override
    public boolean isProfileComplete(String userId) {
        User user = findUserByUuid(userId);
        UserProfile primaryProfile = user.getPrimaryProfile();
        return primaryProfile != null && primaryProfile.isProfileComplete();
    }

    @Override
    @PreAuthorize("@userProfileService.canUserAccessProfileService(#userId)")
    public ProfileCompletionStatus getProfileCompletionStatus(String userId) {
        User user = findUserByUuid(userId);
        UserProfile primaryProfile = user.getPrimaryProfile();

        if (primaryProfile == null) {
            return new ProfileCompletionStatus(false, "No profile found",
                    List.of("Profile needs to be created"));
        }

        List<String> missingFields = getMissingRequiredFields(primaryProfile);
        boolean isComplete = missingFields.isEmpty();
        String message = isComplete ? "Profile is complete" : "Profile is incomplete";

        return new ProfileCompletionStatus(isComplete, message, missingFields);
    }

    @Override
    public void deleteProfile(String userId, UserProfile.ProfileType profileType) {

    }

    @Override
    @PreAuthorize("@userProfileService.canUserAccessProfileService(#userId)")
    public void deleteProfile(String userId, ProfileType profileType) {
        User user = findUserByUuid(userId);

        switch (profileType) {
            case PATIENT -> {
                PatientProfile profile = user.getPatientProfile();
                if (profile != null) {
                    user.removeProfile(profile);
                    // Downgrade role back to PENDING
                    downgradeUserRole(user);
                    userRepository.save(user);
                }
            }
            case DOCTOR -> {
                DoctorProfile profile = user.getDoctorProfile();
                if (profile != null) {
                    user.removeProfile(profile);
                    // Downgrade role back to PENDING
                    downgradeUserRole(user);
                    userRepository.save(user);
                }
            }
            default -> throw new IllegalArgumentException("Unsupported profile type: " + profileType);
        }
    }

    // ========================================================================
    // ACCESS CONTROL METHOD
    // ========================================================================

    /**
     * Checks if user can access profile service (used by @PreAuthorize)
     */
    public boolean canUserAccessProfileService(String userId) {
        try {
            User user = findUserByUuid(userId);
            // Only email-verified users can access profile service
            return user.getMailVerified();
        } catch (Exception e) {
            return false;
        }
    }

    // ========================================================================
    // VALIDATION METHODS
    // ========================================================================

    private void validateUserForProfileCreation(User user, UserProfile.ProfileType profileType) {
        if (!user.getMailVerified()) {
            throw new BadRequestException("email", "Email must be verified before creating profile");
        }

        // Check if user has the required role for the profile type
        if (!user.canCreateProfile(profileType)) {
            throw new BadRequestException("role", 
                    "User does not have the required role to create profile of type " + profileType);
        }

        // Check if user already has this type of profile
        if (user.getPrimaryProfile(profileType) != null) {
            throw new BadRequestException("profile", 
                    "User already has a profile of type " + profileType);
        }
    }

    private void validateUniquePatientFields(CreatePatientProfileRequest request) {
        // Check if NIN already exists
        if (request.getNIN() != null &&
                userRepository.existsByPatientProfileNIN(request.getNIN())) {
            throw new BadRequestException("nin", "NIN already exists");
        }

        // Check if phone number already exists
        if (request.getPhoneNumber() != null &&
                userRepository.existsByProfilePhoneNumber(request.getPhoneNumber())) {
            throw new BadRequestException("phoneNumber", "Phone number already exists");
        }
    }

    private void validateUniqueDoctorFields(CreateDoctorProfileRequest request) {
        // Check if medical license number already exists
        if (request.getMedicalLicenseNumber() != null &&
                userRepository.existsByDoctorProfileMedicalLicenseNumber(request.getMedicalLicenseNumber())) {
            throw new BadRequestException("medicalLicenseNumber", "Medical license number already exists");
        }

        // Check if phone number already exists
        if (request.getPhoneNumber() != null &&
                userRepository.existsByProfilePhoneNumber(request.getPhoneNumber())) {
            throw new BadRequestException("phoneNumber", "Phone number already exists");
        }
    }

    private void validateUniquePatientFieldsForUpdate(PatientProfile existingProfile,
                                                      UpdatePatientProfileRequest request) {
        // Only validate if the field is being changed
        if (request.getNIN() != null &&
                !request.getNIN().equals(existingProfile.getNIN()) &&
                userRepository.existsByPatientProfileNIN(request.getNIN())) {
            throw new BadRequestException("nin", "NIN already exists");
        }

        if (request.getPhoneNumber() != null &&
                !request.getPhoneNumber().equals(existingProfile.getPhoneNumber()) &&
                userRepository.existsByProfilePhoneNumber(request.getPhoneNumber())) {
            throw new BadRequestException("phoneNumber", "Phone number already exists");
        }
    }

    private void validateUniqueDoctorFieldsForUpdate(DoctorProfile existingProfile,
                                                     UpdateDoctorProfileRequest request) {
        // Only validate if the field is being changed
        if (request.getMedicalLicenseNumber() != null &&
                !request.getMedicalLicenseNumber().equals(existingProfile.getMedicalLicenseNumber()) &&
                userRepository.existsByDoctorProfileMedicalLicenseNumber(request.getMedicalLicenseNumber())) {
            throw new BadRequestException("medicalLicenseNumber", "Medical license number already exists");
        }

        if (request.getPhoneNumber() != null &&
                !request.getPhoneNumber().equals(existingProfile.getPhoneNumber()) &&
                userRepository.existsByProfilePhoneNumber(request.getPhoneNumber())) {
            throw new BadRequestException("phoneNumber", "Phone number already exists");
        }
    }

    private void validateUniqueLabTechnicianFields(CreateLabTechnicianProfileRequest request) {
        // Check if certification number already exists
        if (request.getCertificationNumber() != null &&
                userRepository.existsByLabTechnicianProfileCertificationNumber(request.getCertificationNumber())) {
            throw new BadRequestException("certificationNumber", "Certification number already exists");
        }

        // Check if phone number already exists
        if (request.getPhoneNumber() != null &&
                userRepository.existsByProfilePhoneNumber(request.getPhoneNumber())) {
            throw new BadRequestException("phoneNumber", "Phone number already exists");
        }
    }

    private void validateUniqueLabTechnicianFieldsForUpdate(LabTechnicianProfile existingProfile,
                                                           UpdateLabTechnicianProfileRequest request) {
        // Only validate if the field is being changed
        if (request.getCertificationNumber() != null &&
                !request.getCertificationNumber().equals(existingProfile.getCertificationNumber()) &&
                userRepository.existsByLabTechnicianProfileCertificationNumber(request.getCertificationNumber())) {
            throw new BadRequestException("certificationNumber", "Certification number already exists");
        }

        if (request.getPhoneNumber() != null &&
                !request.getPhoneNumber().equals(existingProfile.getPhoneNumber()) &&
                userRepository.existsByProfilePhoneNumber(request.getPhoneNumber())) {
            throw new BadRequestException("phoneNumber", "Phone number already exists");
        }
    }

    // ========================================================================
    // PROFILE UPDATE HELPER METHODS
    // ========================================================================

    private void updatePatientProfileFields(PatientProfile profile, UpdatePatientProfileRequest request) {
        if (request.getDateOfBirth() != null) {
            profile.setDateOfBirth(request.getDateOfBirth());
        }
        if (request.getGender() != null) {
            profile.setGender(request.getGender());
        }
        if (request.getNIN() != null) {
            profile.setNIN(request.getNIN());
        }
        if (request.getMaritalStatus() != null) {
            profile.setMaritalStatus(request.getMaritalStatus());
        }
        if (request.getBloodType() != null) {
            profile.setBloodType(request.getBloodType());
        }
        if (request.getPreferredHospital() != null) {
            profile.setPreferredHospital(request.getPreferredHospital());
        }
        if (request.getEmergencyContactName() != null) {
            profile.setEmergencyContactName(request.getEmergencyContactName());
        }
        if (request.getEmergencyContactPhone() != null) {
            profile.setEmergencyContactPhone(request.getEmergencyContactPhone());
        }
        if (request.getPhoneNumber() != null) {
            profile.setPhoneNumber(request.getPhoneNumber());
        }
        if (request.getAddress() != null) {
            profile.setAddress(request.getAddress());
        }
        if (request.getProfileImageUrl() != null) {
            profile.setProfileImageUrl(request.getProfileImageUrl());
        }
    }

    private void updateDoctorProfileFields(DoctorProfile profile, UpdateDoctorProfileRequest request) {
        if (request.getMedicalLicenseNumber() != null) {
            profile.setMedicalLicenseNumber(request.getMedicalLicenseNumber());
        }
        if (request.getSpecialization() != null) {
            profile.setSpecialization(request.getSpecialization());
        }
        if (request.getYearsOfExperience() != null) {
            profile.setYearsOfExperience(request.getYearsOfExperience());
        }
        if (request.getHospitalAffiliation() != null) {
            profile.setHospitalAffiliation(request.getHospitalAffiliation());
        }
//        if (request.getConsultationFee() != null) {
//            profile.setConsultationFee(request.getConsultationFee());
//        }
        if (request.getAvailableForConsultation() != null) {
            profile.setAvailableForConsultation(request.getAvailableForConsultation());
        }
        if (request.getOfficeHours() != null) {
            profile.setOfficeHours(request.getOfficeHours());
        }
        if (request.getMedicalSchool() != null) {
            profile.setMedicalSchool(request.getMedicalSchool());
        }
        if (request.getBoardCertifications() != null) {
            profile.setBoardCertifications(request.getBoardCertifications());
        }
        if (request.getPhoneNumber() != null) {
            profile.setPhoneNumber(request.getPhoneNumber());
        }
        if (request.getAddress() != null) {
            profile.setAddress(request.getAddress());
        }
        if (request.getProfileImageUrl() != null) {
            profile.setProfileImageUrl(request.getProfileImageUrl());
        }
    }

    // ========================================================================
    // ROLE MANAGEMENT METHODS
    // ========================================================================

    private void checkAndUpgradeUserRole(User user) {
        if (!user.getMailVerified()) {
            return; // Email must be verified first
        }

        UserProfile primaryProfile = user.getPrimaryProfile();
        if (primaryProfile != null && primaryProfile.isProfileComplete()) {
            // Profile is complete, upgrade from PENDING role
            Role.RoleName newRole = determineRoleFromAccountType(user.getAccountType());
            if (newRole != null && !user.getAccountVerified()) {
                // Remove PENDING role and assign appropriate role
                rolePermissionService.removeRoleFromUser(user.getId(), Role.RoleName.PENDING);
                rolePermissionService.assignRoleToUser(user.getId(), newRole);

                // Mark account as fully verified
                user.setAccountVerified(true);
            }
        }
    }

    private void downgradeUserRole(User user) {
        Role.RoleName currentRole = determineRoleFromAccountType(user.getAccountType());
        if (currentRole != null) {
            rolePermissionService.removeRoleFromUser(user.getId(), currentRole);
            rolePermissionService.assignRoleToUser(user.getId(), Role.RoleName.PENDING);
            user.setAccountVerified(false);
        }
    }

    private Role.RoleName determineRoleFromAccountType(User.AccountType accountType) {
        return switch (accountType) {
            case DOCTOR -> Role.RoleName.DOCTOR;
            case PATIENT -> Role.RoleName.PATIENT;
            case ADMIN -> Role.RoleName.ADMIN;
            case PHARMACY -> Role.RoleName.PHARMACY_ADMIN;
            case HOSPITAL -> Role.RoleName.HOSPITAL_ADMIN;
            case INSURANCE -> Role.RoleName.INSURANCE_ADMIN;
            case LAB_TECHNICIAN -> Role.RoleName.LAB_TECHNICIAN;
            default -> null;
        };
    }

    // ========================================================================
    // HELPER METHODS
    // ========================================================================

    private List<String> getMissingRequiredFields(UserProfile profile) {
        List<String> missingFields = new ArrayList<>();

        // Common fields
        if (profile.getPhoneNumber() == null || profile.getPhoneNumber().trim().isEmpty()) {
            missingFields.add("Phone Number");
        }
        if (profile.getAddress() == null || profile.getAddress().trim().isEmpty()) {
            missingFields.add("Address");
        }

        // Profile-specific fields
        if (profile instanceof PatientProfile patient) {
            if (patient.getDateOfBirth() == null) {
                missingFields.add("Date of Birth");
            }
            if (patient.getGender() == null) {
                missingFields.add("Gender");
            }
            if (patient.getNIN() == null || patient.getNIN().trim().isEmpty()) {
                missingFields.add("National Identification Number");
            }
        } else if (profile instanceof DoctorProfile doctor) {
            if (doctor.getMedicalLicenseNumber() == null || doctor.getMedicalLicenseNumber().trim().isEmpty()) {
                missingFields.add("Medical License Number");
            }
            if (doctor.getSpecialization() == null || doctor.getSpecialization().trim().isEmpty()) {
                missingFields.add("Specialization");
            }
            if (doctor.getHospitalAffiliation() == null || doctor.getHospitalAffiliation().trim().isEmpty()) {
                missingFields.add("Hospital Affiliation");
            }
        }

        return missingFields;
    }

    private User findUserByUuid(String userUuid) {
        return userRepository.findAll().stream()
                .filter(user -> userUuid.equals(user.getUuid()))
                .findFirst()
                .orElseThrow(() -> new UsernameNotFoundException("User not found with UUID: " + userUuid));
    }

    // ========================================================================
    // SUPPORTING CLASSES
    // ========================================================================

//    public static class ProfileCompletionStatus {
//        private final boolean complete;
//        private final String message;
//        private final List<String> missingFields;
//
//        public ProfileCompletionStatus(boolean complete, String message, List<String> missingFields) {
//            this.complete = complete;
//            this.message = message;
//            this.missingFields = missingFields;
//        }
//
//        public boolean isComplete() { return complete; }
//        public String getMessage() { return message; }
//        public List<String> getMissingFields() { return missingFields; }
//    }

    public enum ProfileType {
        PATIENT, DOCTOR, ADMIN, PHARMACY_OWNER
    }
}
