package com.auth.service.service;

import com.auth.service.dto.*;
import com.auth.service.entity.UserProfile;
import com.auth.service.utils.ProfileCompletionStatus;
import org.springframework.security.access.prepost.PreAuthorize;

public interface UserProfileService {
    // Profile Creation
    PatientProfileDTO createPatientProfile(String userId, CreatePatientProfileRequest request);
    DoctorProfileDTO createDoctorProfile(String userId, CreateDoctorProfileRequest request);
    LabTechnicianProfileDTO createLabTechnicianProfile(String userId, CreateLabTechnicianProfileRequest request);

    // Profile Updates
    PatientProfileDTO updatePatientProfile(String userId, UpdatePatientProfileRequest request);
    DoctorProfileDTO updateDoctorProfile(String userId, UpdateDoctorProfileRequest request);
    LabTechnicianProfileDTO updateLabTechnicianProfile(String userId, UpdateLabTechnicianProfileRequest request);

    // Profile Retrieval
    UserProfileDTO getUserProfile(String userId);
    PatientProfileDTO getPatientProfile(String userId);
    DoctorProfileDTO getDoctorProfile(String userId);
    LabTechnicianProfileDTO getLabTechnicianProfile(String userId);

    // Profile Status
    boolean hasProfile(String userId);
    boolean isProfileComplete(String userId);
    ProfileCompletionStatus getProfileCompletionStatus(String userId);

    // Profile Deletion (if needed)
    void deleteProfile(String userId, UserProfile.ProfileType profileType);

    // Access Control
    boolean canUserAccessProfileService(String userId);
}
