package com.mths.auth.service;

import com.mths.auth.dto.*;
import com.mths.patient.dto.*;
import com.mths.hospital.dto.*;
import com.mths.pharmacy.dto.*;
import com.mths.patient.entity.PatientProfile;
import com.mths.hospital.entity.DoctorProfile;
import com.mths.hospital.entity.LabTechnicianProfile;
import com.mths.pharmacy.entity.Pharmacist;
import com.mths.pharmacy.entity.Pharmacy;
import com.mths.auth.entity.*;
import com.mths.shared.exceptions.BadRequestException;
import com.mths.shared.exceptions.ResourceNotFoundException;
import com.mths.shared.mapper.ProfileMapper;
import com.mths.shared.mapper.LabTechnicianMapper;
import com.mths.auth.repository.UserRepository;
import com.mths.pharmacy.repository.PharmacistRepository;
import com.mths.pharmacy.repository.PharmacyRepository;
import com.mths.shared.utils.ProfileCompletionStatus;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
@Slf4j
public class UserProfileServiceImpl implements UserProfileService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PharmacistRepository pharmacistRepository;

    @Autowired
    private PharmacyRepository pharmacyRepository;

    @Autowired
    private RolePermissionService rolePermissionService;

    @Autowired
    private ProfileMapper profileMapper;

    @Autowired
    private LabTechnicianMapper labTechnicianMapper;

    @Autowired
    private UserAuthService userAuthService;

    // ========================================================================
    // PROFILE CREATION METHODS
    // ========================================================================


    @Override
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

        // Notify AuthService about profile completion change (for tracking only)
        notifyProfileCompletionChange(savedUser);

        return profileMapper.toPatientProfileDTO(savedUser.getPatientProfile());
    }

    @Override
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
        doctorProfile.setExperience(request.getExperience());
        doctorProfile.setBio(request.getBio());
        doctorProfile.setPracticeAddress(request.getPracticeAddress());
        doctorProfile.setDateOfBirth(request.getDateOfBirth());
        doctorProfile.setGender(request.getGender());
        doctorProfile.setCertificateUrl(request.getCertificateUrl());
        doctorProfile.setBoardCertifications(request.getBoardCertifications());

        // Set common profile fields
        doctorProfile.setPhoneNumber(request.getPhoneNumber());
        doctorProfile.setAddress(request.getAddress());
        doctorProfile.setProfileImageUrl(request.getProfileImageUrl());

        // Add profile to user
        user.addProfile(doctorProfile);

        // Save user with profile
        User savedUser = userRepository.save(user);

        // Notify AuthService about profile completion change (for tracking only)
        notifyProfileCompletionChange(savedUser);

        return profileMapper.toDoctorProfileDTO(savedUser.getDoctorProfile());
    }

    @Override
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

        // Notify AuthService about profile completion change (for tracking only)
        notifyProfileCompletionChange(savedUser);

        return labTechnicianMapper.toDTO(savedUser.getLabTechnicianProfile());
    }

    @Override
    public PharmacistProfileDTO createPharmacistProfile(String userId, CreatePharmacistProfileRequest request) {
        log.info("Creating pharmacist profile for user: {}", userId);

        User user = findUserByUuid(userId);
        validateUserForProfileCreation(user, UserProfile.ProfileType.PHARMACIST);

        if (user.hasPharmacistProfile()) {
            throw new BadRequestException("profile", "Pharmacist profile already exists for this user");
        }

        // Validate unique fields
        validateUniquePharmacistFields(request);

        // Get pharmacy if provided
        Pharmacy pharmacy = null;
        if (request.getPharmacyId() != null) {
            pharmacy = pharmacyRepository.findById(request.getPharmacyId())
                    .orElseThrow(() -> new ResourceNotFoundException("Pharmacy", "Pharmacy not found", request.getPharmacyId().toString()));
        }

        // Create pharmacist profile
        Pharmacist pharmacist = new Pharmacist();
        pharmacist.setPharmacy(pharmacy);
        pharmacist.setLicenseNumber(request.getLicenseNumber());
        pharmacist.setRegistrationNumber(request.getRegistrationNumber());
        pharmacist.setLicenseExpiryDate(request.getLicenseExpiryDate());
        pharmacist.setIssuingAuthority(request.getIssuingAuthority());
        pharmacist.setGender(request.getGender());
        pharmacist.setDateOfBirth(request.getDateOfBirth());
        pharmacist.setPhoneNumber(request.getPhoneNumber());
        pharmacist.setAlternativePhone(request.getAlternativePhone());
        pharmacist.setAddress(request.getAddress());
        pharmacist.setSpecialization(request.getSpecialization());
        pharmacist.setYearsOfExperience(request.getYearsOfExperience());
        pharmacist.setQualifications(request.getQualifications());
        pharmacist.setPharmacySchool(request.getPharmacySchool());
        pharmacist.setGraduationYear(request.getGraduationYear());
        pharmacist.setEmploymentType(request.getEmploymentType());
        pharmacist.setPosition(request.getPosition());
        pharmacist.setHireDate(request.getHireDate());
        pharmacist.setIsSuperintendent(request.getIsSuperintendent());
        pharmacist.setBio(request.getBio());
        pharmacist.setCertificateUrl(request.getCertificateUrl());

        // Add profile to user
        user.addProfile(pharmacist);

        // Save user with profile
        User savedUser = userRepository.save(user);

        // Notify AuthService about profile completion change
        notifyProfileCompletionChange(savedUser);

        return profileMapper.toPharmacistProfileDTO(savedUser.getPharmacistProfile());
    }

    // ========================================================================
    // PROFILE UPDATE METHODS
    // ========================================================================

    @Override
    public PatientProfileDTO updatePatientProfile(String userId, UpdatePatientProfileRequest request) {
        User user = findUserByUuid(userId);
        validateEmailVerified(user);
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

        // Notify AuthService about profile completion change (for tracking only)
        notifyProfileCompletionChange(savedUser);

        return profileMapper.toPatientProfileDTO(savedUser.getPatientProfile());
    }

    @Override
    public DoctorProfileDTO updateDoctorProfile(String userId, UpdateDoctorProfileRequest request) {
        User user = findUserByUuid(userId);
        validateEmailVerified(user);
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

        // Notify AuthService about profile completion change (for tracking only)
        notifyProfileCompletionChange(savedUser);

        return profileMapper.toDoctorProfileDTO(savedUser.getDoctorProfile());
    }

    @Override
    public LabTechnicianProfileDTO updateLabTechnicianProfile(String userId, UpdateLabTechnicianProfileRequest request) {
        User user = findUserByUuid(userId);
        validateEmailVerified(user);
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

        // Notify AuthService about profile completion change (for tracking only)
        notifyProfileCompletionChange(savedUser);

        return labTechnicianMapper.toDTO(savedUser.getLabTechnicianProfile());
    }

    @Override
    public PharmacistProfileDTO updatePharmacistProfile(String userId, UpdatePharmacistProfileRequest request) {
        log.info("Updating pharmacist profile for user: {}", userId);

        User user = findUserByUuid(userId);
        validateEmailVerified(user);
        Pharmacist pharmacist = user.getPharmacistProfile();

        if (pharmacist == null) {
            throw new ResourceNotFoundException("Profile", "Pharmacist profile not found for user", userId);
        }

        // Update pharmacy if provided
        if (request.getPharmacyId() != null) {
            Pharmacy pharmacy = pharmacyRepository.findById(request.getPharmacyId())
                    .orElseThrow(() -> new ResourceNotFoundException("Pharmacy", "Pharmacy not found", request.getPharmacyId().toString()));
            pharmacist.setPharmacy(pharmacy);
        }

        // Update fields (only if not null)
        updatePharmacistProfileFields(pharmacist, request);

        // Save updated profile
        User savedUser = userRepository.save(user);

        // Notify AuthService about profile completion change
        notifyProfileCompletionChange(savedUser);

        return profileMapper.toPharmacistProfileDTO(savedUser.getPharmacistProfile());
    }

    // ========================================================================
    // PROFILE RETRIEVAL METHODS
    // ========================================================================

    @Override
    public UserProfileDTO getUserProfile(String userId) {
        User user = findUserByUuid(userId);
        validateEmailVerified(user);

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
    public PatientProfileDTO getPatientProfile(String userId) {
        User user = findUserByUuid(userId);
        validateEmailVerified(user);
        PatientProfile profile = user.getPatientProfile();

        if (profile == null) {
            throw new ResourceNotFoundException("Profile", "Patient profile not found for user", userId);
        }

        return profileMapper.toPatientProfileDTO(profile);
    }

    @Override
    public DoctorProfileDTO getDoctorProfile(String userId) {
        User user = findUserByUuid(userId);
        validateEmailVerified(user);
        DoctorProfile profile = user.getDoctorProfile();

        if (profile == null) {
            throw new ResourceNotFoundException("Profile", "Doctor profile not found for user", userId);
        }

        return profileMapper.toDoctorProfileDTO(profile);
    }

    @Override
    public LabTechnicianProfileDTO getLabTechnicianProfile(String userId) {
        User user = findUserByUuid(userId);
        validateEmailVerified(user);
        LabTechnicianProfile profile = user.getLabTechnicianProfile();

        if (profile == null) {
            throw new ResourceNotFoundException("Profile", "Lab technician profile not found for user", userId);
        }

        return labTechnicianMapper.toDTO(profile);
    }

    @Override
    public PharmacistProfileDTO getPharmacistProfile(String userId) {
        log.info("Retrieving pharmacist profile for user: {}", userId);

        User user = findUserByUuid(userId);
        validateEmailVerified(user);
        Pharmacist pharmacist = user.getPharmacistProfile();

        if (pharmacist == null) {
            throw new ResourceNotFoundException("Profile", "Pharmacist profile not found for user", userId);
        }

        return profileMapper.toPharmacistProfileDTO(pharmacist);
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
    public ProfileCompletionStatus getProfileCompletionStatus(String userId) {
        User user = findUserByUuid(userId);
        validateEmailVerified(user);
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


    // ========================================================================
    // ACCESS CONTROL METHOD
    // ========================================================================

    /**
     * Checks if user can access profile service (kept for potential future use)
     */
    public boolean canUserAccessProfileService(String userId) {
        try {
            User user = findUserByUuid(userId);
            // Only email-verified and account-verified users can access profile service
            return user.getMailVerified() && user.getAccountVerified();
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Validates that a user's email and account are verified, throwing exception if not
     * ADMIN users are exempt from this validation
     */
    private void validateEmailVerified(User user) {
        // ADMIN users can access profile services without verification
        if (user.getAccountType() == User.AccountType.ADMIN) {
            return;
        }

        if (!user.getMailVerified()) {
            throw new BadRequestException("email", "Email must be verified to access profile services");
        }
        if (!user.getAccountVerified()) {
            throw new BadRequestException("account", "Account must be verified to access profile services");
        }
    }

    // ========================================================================
    // VALIDATION METHODS
    // ========================================================================

    private void validateUserForProfileCreation(User user, UserProfile.ProfileType profileType) {
        if (!user.getMailVerified()) {
            throw new BadRequestException("email", "Email must be verified before creating profile");
        }

        // Check if user's account type matches the profile type they want to create
        boolean canCreate = switch (profileType) {
            case PATIENT -> user.getAccountType() == User.AccountType.PATIENT;
            case DOCTOR -> user.getAccountType() == User.AccountType.DOCTOR;
            case LAB_TECHNICIAN -> user.getAccountType() == User.AccountType.LAB_TECHNICIAN;
            case PHARMACIST -> user.getAccountType() == User.AccountType.PHARMACIST ||
                              user.getAccountType() == User.AccountType.PHARMACY;
            default -> false;
        };

        if (!canCreate) {
            throw new BadRequestException("accountType", 
                    "User account type does not match profile type " + profileType + 
                    ". Expected account type: " + profileType.name());
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

    private void validateUniquePharmacistFields(CreatePharmacistProfileRequest request) {
        // Check if license number already exists
        if (request.getLicenseNumber() != null &&
                pharmacistRepository.existsByLicenseNumber(request.getLicenseNumber())) {
            throw new BadRequestException("licenseNumber", "License number already exists");
        }

        // Check if registration number already exists
        if (request.getRegistrationNumber() != null &&
                pharmacistRepository.existsByRegistrationNumber(request.getRegistrationNumber())) {
            throw new BadRequestException("registrationNumber", "Registration number already exists");
        }

        // Check if phone number already exists
        if (request.getPhoneNumber() != null &&
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
        if (request.getExperience() != null) {
            profile.setExperience(request.getExperience());
        }
        if (request.getBio() != null) {
            profile.setBio(request.getBio());
        }
        if (request.getPracticeAddress() != null) {
            profile.setPracticeAddress(request.getPracticeAddress());
        }
        if (request.getDateOfBirth() != null) {
            profile.setDateOfBirth(request.getDateOfBirth());
        }
        if (request.getGender() != null) {
            profile.setGender(request.getGender());
        }
        if (request.getCertificateUrl() != null) {
            profile.setCertificateUrl(request.getCertificateUrl());
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

    private void updatePharmacistProfileFields(Pharmacist pharmacist, UpdatePharmacistProfileRequest request) {
        if (request.getLicenseExpiryDate() != null) {
            pharmacist.setLicenseExpiryDate(request.getLicenseExpiryDate());
        }
        if (request.getIssuingAuthority() != null) {
            pharmacist.setIssuingAuthority(request.getIssuingAuthority());
        }
        if (request.getGender() != null) {
            pharmacist.setGender(request.getGender());
        }
        if (request.getDateOfBirth() != null) {
            pharmacist.setDateOfBirth(request.getDateOfBirth());
        }
        if (request.getPhoneNumber() != null) {
            pharmacist.setPhoneNumber(request.getPhoneNumber());
        }
        if (request.getAlternativePhone() != null) {
            pharmacist.setAlternativePhone(request.getAlternativePhone());
        }
        if (request.getAddress() != null) {
            pharmacist.setAddress(request.getAddress());
        }
        if (request.getSpecialization() != null) {
            pharmacist.setSpecialization(request.getSpecialization());
        }
        if (request.getYearsOfExperience() != null) {
            pharmacist.setYearsOfExperience(request.getYearsOfExperience());
        }
        if (request.getQualifications() != null) {
            pharmacist.setQualifications(request.getQualifications());
        }
        if (request.getPharmacySchool() != null) {
            pharmacist.setPharmacySchool(request.getPharmacySchool());
        }
        if (request.getGraduationYear() != null) {
            pharmacist.setGraduationYear(request.getGraduationYear());
        }
        if (request.getEmploymentType() != null) {
            pharmacist.setEmploymentType(request.getEmploymentType());
        }
        if (request.getPosition() != null) {
            pharmacist.setPosition(request.getPosition());
        }
        if (request.getHireDate() != null) {
            pharmacist.setHireDate(request.getHireDate());
        }
        if (request.getIsSuperintendent() != null) {
            pharmacist.setIsSuperintendent(request.getIsSuperintendent());
        }
        if (request.getIsAvailable() != null) {
            pharmacist.setIsAvailable(request.getIsAvailable());
        }
        if (request.getAvailabilityNotes() != null) {
            pharmacist.setAvailabilityNotes(request.getAvailabilityNotes());
        }
        if (request.getBio() != null) {
            pharmacist.setBio(request.getBio());
        }
        if (request.getCertificateUrl() != null) {
            pharmacist.setCertificateUrl(request.getCertificateUrl());
        }
    }

    // ========================================================================
    // ROLE MANAGEMENT METHODS
    // ========================================================================

    /**
     * Notify AuthService about profile completion changes without auto-verification
     */
    private void notifyProfileCompletionChange(User user) {
        // Delegate to UserAuthService which handles profile completion tracking
        // without automatic account verification
        userAuthService.handleProfileCompletionChange(user.getUuid());
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
            case PHARMACIST -> Role.RoleName.PHARMACIST;
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
            if (doctor.getPracticeAddress() == null || doctor.getPracticeAddress().trim().isEmpty()) {
                missingFields.add("Practice Address");
            }
            if (doctor.getDateOfBirth() == null) {
                missingFields.add("Date of Birth");
            }
            if (doctor.getGender() == null) {
                missingFields.add("Gender");
            }
        }

        return missingFields;
    }

    private User findUserByUuid(String userIdentifier) {
        // Check if the identifier is an email (contains @) or a UUID
        if (userIdentifier.contains("@")) {
            // It's an email, find by email
            return userRepository.findByEmail(userIdentifier)
                    .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + userIdentifier));
        } else {
            // It's a UUID, find by UUID
            return userRepository.findAll().stream()
                    .filter(user -> userIdentifier.equals(user.getUuid()))
                    .findFirst()
                    .orElseThrow(() -> new UsernameNotFoundException("User not found with UUID: " + userIdentifier));
        }
    }


    public enum ProfileType {
        PATIENT, DOCTOR, ADMIN, PHARMACY_OWNER
    }
}
