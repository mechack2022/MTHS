package com.auth.service.controller;

import com.auth.service.dto.*;
import com.auth.service.service.UserProfileService;
import com.auth.service.entity.UserProfile.ProfileType;
import com.auth.service.utils.ProfileCompletionStatus;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/v1/profile/")
@Slf4j
@AllArgsConstructor
public class UserProfileController {

    private final UserProfileService userProfileService;

    // ========================================================================
    // PROFILE CREATION ENDPOINTS (for authenticated users)
    // ========================================================================

    @PostMapping("/patient")
    public ResponseEntity<ApiResponse<PatientProfileDTO>> createPatientProfile(
            @Valid @RequestBody CreatePatientProfileRequest request,
            @RequestParam(required = false) String targetUserId) {
        String userId = determineTargetUserId(targetUserId);
        log.info("Creating patient profile for user: {} by: {}", userId, getCurrentUserId());

        PatientProfileDTO profile = userProfileService.createPatientProfile(userId, request);

        ApiResponse<PatientProfileDTO> response = ApiResponse.success(
                "Patient profile created successfully",
                profile,
                HttpStatus.CREATED.value()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/doctor")
    public ResponseEntity<ApiResponse<DoctorProfileDTO>> createDoctorProfile(
            @Valid @RequestBody CreateDoctorProfileRequest request,
            @RequestParam(required = false) String targetUserId) {
        String userId = determineTargetUserId(targetUserId);
        log.info("Creating doctor profile for user: {} by: {}", userId, getCurrentUserId());

        DoctorProfileDTO profile = userProfileService.createDoctorProfile(userId, request);

        ApiResponse<DoctorProfileDTO> response = ApiResponse.success(
                "Doctor profile created successfully",
                profile,
                HttpStatus.CREATED.value()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/lab-technician")
    public ResponseEntity<ApiResponse<LabTechnicianProfileDTO>> createLabTechnicianProfile(
            @Valid @RequestBody CreateLabTechnicianProfileRequest request,
            @RequestParam(required = false) String targetUserId) {
        String userId = determineTargetUserId(targetUserId);
        log.info("Creating lab technician profile for user: {} by: {}", userId, getCurrentUserId());

        LabTechnicianProfileDTO profile = userProfileService.createLabTechnicianProfile(userId, request);

        ApiResponse<LabTechnicianProfileDTO> response = ApiResponse.success(
                "Lab technician profile created successfully",
                profile,
                HttpStatus.CREATED.value()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // ========================================================================
    // PROFILE CREATION ENDPOINTS (for unauthenticated admin operations)
    // ========================================================================

    @PostMapping("/admin/patient/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<PatientProfileDTO>> createPatientProfileByAdmin(
            @PathVariable String userId,
            @Valid @RequestBody CreatePatientProfileRequest request) {
        log.info("Admin creating patient profile for user: {} by: {}", userId, getCurrentUserIdSafe());

        PatientProfileDTO profile = userProfileService.createPatientProfile(userId, request);

        ApiResponse<PatientProfileDTO> response = ApiResponse.success(
                "Patient profile created successfully by admin",
                profile,
                HttpStatus.CREATED.value()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/admin/doctor/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<DoctorProfileDTO>> createDoctorProfileByAdmin(
            @PathVariable String userId,
            @Valid @RequestBody CreateDoctorProfileRequest request) {
        log.info("Admin creating doctor profile for user: {} by: {}", userId, getCurrentUserIdSafe());

        DoctorProfileDTO profile = userProfileService.createDoctorProfile(userId, request);

        ApiResponse<DoctorProfileDTO> response = ApiResponse.success(
                "Doctor profile created successfully by admin",
                profile,
                HttpStatus.CREATED.value()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/admin/lab-technician/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<LabTechnicianProfileDTO>> createLabTechnicianProfileByAdmin(
            @PathVariable String userId,
            @Valid @RequestBody CreateLabTechnicianProfileRequest request) {
        log.info("Admin creating lab technician profile for user: {} by: {}", userId, getCurrentUserIdSafe());

        LabTechnicianProfileDTO profile = userProfileService.createLabTechnicianProfile(userId, request);

        ApiResponse<LabTechnicianProfileDTO> response = ApiResponse.success(
                "Lab technician profile created successfully by admin",
                profile,
                HttpStatus.CREATED.value()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // ========================================================================
    // PROFILE UPDATE ENDPOINTS
    // ========================================================================

    @PutMapping("/patient")
    public ResponseEntity<ApiResponse<PatientProfileDTO>> updatePatientProfile(
            @Valid @RequestBody UpdatePatientProfileRequest request,
            @RequestParam(required = false) String targetUserId) {
        String userId = determineTargetUserId(targetUserId);
        log.info("Updating patient profile for user: {} by: {}", userId, getCurrentUserId());

        PatientProfileDTO profile = userProfileService.updatePatientProfile(userId, request);

        ApiResponse<PatientProfileDTO> response = ApiResponse.success(
                "Patient profile updated successfully",
                profile
        );
        return ResponseEntity.ok(response);
    }

    @PutMapping("/doctor")
    public ResponseEntity<ApiResponse<DoctorProfileDTO>> updateDoctorProfile(
            @Valid @RequestBody UpdateDoctorProfileRequest request,
            @RequestParam(required = false) String targetUserId) {
        String userId = determineTargetUserId(targetUserId);
        log.info("Updating doctor profile for user: {} by: {}", userId, getCurrentUserId());

        DoctorProfileDTO profile = userProfileService.updateDoctorProfile(userId, request);

        ApiResponse<DoctorProfileDTO> response = ApiResponse.success(
                "Doctor profile updated successfully",
                profile
        );
        return ResponseEntity.ok(response);
    }

    @PutMapping("/lab-technician")
    public ResponseEntity<ApiResponse<LabTechnicianProfileDTO>> updateLabTechnicianProfile(
            @Valid @RequestBody UpdateLabTechnicianProfileRequest request,
            @RequestParam(required = false) String targetUserId) {
        String userId = determineTargetUserId(targetUserId);
        log.info("Updating lab technician profile for user: {} by: {}", userId, getCurrentUserId());

        LabTechnicianProfileDTO profile = userProfileService.updateLabTechnicianProfile(userId, request);

        ApiResponse<LabTechnicianProfileDTO> response = ApiResponse.success(
                "Lab technician profile updated successfully",
                profile
        );
        return ResponseEntity.ok(response);
    }

    // ========================================================================
    // PROFILE RETRIEVAL ENDPOINTS
    // ========================================================================

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserProfileDTO>> getCurrentUserProfile() {
        String userId = getCurrentUserId();
        log.info("Retrieving profile for user: {}", userId);

        UserProfileDTO profile = userProfileService.getUserProfile(userId);

        ApiResponse<UserProfileDTO> response = ApiResponse.success(
                "Profile retrieved successfully",
                profile
        );
        return ResponseEntity.ok(response);
    }

    @GetMapping("/patient")
    public ResponseEntity<ApiResponse<PatientProfileDTO>> getCurrentPatientProfile() {
        String userId = getCurrentUserId();
        log.info("Retrieving patient profile for user: {}", userId);

        PatientProfileDTO profile = userProfileService.getPatientProfile(userId);

        ApiResponse<PatientProfileDTO> response = ApiResponse.success(
                "Patient profile retrieved successfully",
                profile
        );
        return ResponseEntity.ok(response);
    }

    @GetMapping("/doctor")
    public ResponseEntity<ApiResponse<DoctorProfileDTO>> getCurrentDoctorProfile() {
        String userId = getCurrentUserId();
        log.info("Retrieving doctor profile for user: {}", userId);

        DoctorProfileDTO profile = userProfileService.getDoctorProfile(userId);

        ApiResponse<DoctorProfileDTO> response = ApiResponse.success(
                "Doctor profile retrieved successfully",
                profile
        );
        return ResponseEntity.ok(response);
    }

    @GetMapping("/lab-technician")
    public ResponseEntity<ApiResponse<LabTechnicianProfileDTO>> getCurrentLabTechnicianProfile() {
        String userId = getCurrentUserId();
        log.info("Retrieving lab technician profile for user: {}", userId);

        LabTechnicianProfileDTO profile = userProfileService.getLabTechnicianProfile(userId);

        ApiResponse<LabTechnicianProfileDTO> response = ApiResponse.success(
                "Lab technician profile retrieved successfully",
                profile
        );
        return ResponseEntity.ok(response);
    }

    // ========================================================================
    // PROFILE STATUS ENDPOINTS
    // ========================================================================

    @GetMapping("/status/has-profile")
    public ResponseEntity<ApiResponse<Boolean>> hasProfile() {
        String userId = getCurrentUserId();
        log.info("Checking if user has profile: {}", userId);

        boolean hasProfile = userProfileService.hasProfile(userId);

        ApiResponse<Boolean> response = ApiResponse.success(
                hasProfile ? "User has a profile" : "User does not have a profile",
                hasProfile
        );
        return ResponseEntity.ok(response);
    }

    @GetMapping("/status/is-complete")
    public ResponseEntity<ApiResponse<Boolean>> isProfileComplete() {
        String userId = getCurrentUserId();
        log.info("Checking if profile is complete for user: {}", userId);

        boolean isComplete = userProfileService.isProfileComplete(userId);

        ApiResponse<Boolean> response = ApiResponse.success(
                isComplete ? "Profile is complete" : "Profile is incomplete",
                isComplete
        );
        return ResponseEntity.ok(response);
    }

    @GetMapping("/status/completion")
    public ResponseEntity<ApiResponse<ProfileCompletionStatus>> getProfileCompletionStatus() {
        String userId = getCurrentUserId();
        log.info("Getting profile completion status for user: {}", userId);

        ProfileCompletionStatus status = userProfileService.getProfileCompletionStatus(userId);

        ApiResponse<ProfileCompletionStatus> response = ApiResponse.success(
                "Profile completion status retrieved successfully",
                status
        );
        return ResponseEntity.ok(response);
    }

    // ========================================================================
    // PROFILE DELETION ENDPOINTS
    // ========================================================================

    @DeleteMapping("/patient")
    public ResponseEntity<ApiResponse<String>> deletePatientProfile(
            @RequestParam(required = false) String targetUserId) {
        String userId = determineTargetUserId(targetUserId);
        log.info("Deleting patient profile for user: {} by: {}", userId, getCurrentUserId());

        userProfileService.deleteProfile(userId, ProfileType.PATIENT);

        ApiResponse<String> response = ApiResponse.success(
                "Patient profile deleted successfully",
                "Profile deleted"
        );
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/doctor")
    public ResponseEntity<ApiResponse<String>> deleteDoctorProfile(
            @RequestParam(required = false) String targetUserId) {
        String userId = determineTargetUserId(targetUserId);
        log.info("Deleting doctor profile for user: {} by: {}", userId, getCurrentUserId());

        userProfileService.deleteProfile(userId, ProfileType.DOCTOR);

        ApiResponse<String> response = ApiResponse.success(
                "Doctor profile deleted successfully",
                "Profile deleted"
        );
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/lab-technician")
    public ResponseEntity<ApiResponse<String>> deleteLabTechnicianProfile(
            @RequestParam(required = false) String targetUserId) {
        String userId = determineTargetUserId(targetUserId);
        log.info("Deleting lab technician profile for user: {} by: {}", userId, getCurrentUserId());

        userProfileService.deleteProfile(userId, ProfileType.LAB_TECHNICIAN);

        ApiResponse<String> response = ApiResponse.success(
                "Lab technician profile deleted successfully",
                "Profile deleted"
        );
        return ResponseEntity.ok(response);
    }

    // ========================================================================
    // ADMIN ENDPOINTS (Optional - for viewing other users' profiles)
    // ========================================================================

    @GetMapping("/user/{userId}")
    public ResponseEntity<ApiResponse<UserProfileDTO>> getUserProfile(@PathVariable String userId) {
        log.info("Admin retrieving profile for user: {}", userId);

        UserProfileDTO profile = userProfileService.getUserProfile(userId);

        ApiResponse<UserProfileDTO> response = ApiResponse.success(
                "User profile retrieved successfully",
                profile
        );
        return ResponseEntity.ok(response);
    }

    @GetMapping("/user/{userId}/patient")
    public ResponseEntity<ApiResponse<PatientProfileDTO>> getUserPatientProfile(@PathVariable String userId) {
        log.info("Admin retrieving patient profile for user: {}", userId);

        PatientProfileDTO profile = userProfileService.getPatientProfile(userId);

        ApiResponse<PatientProfileDTO> response = ApiResponse.success(
                "Patient profile retrieved successfully",
                profile
        );
        return ResponseEntity.ok(response);
    }

    @GetMapping("/user/{userId}/doctor")
    public ResponseEntity<ApiResponse<DoctorProfileDTO>> getUserDoctorProfile(@PathVariable String userId) {
        log.info("Admin retrieving doctor profile for user: {}", userId);

        DoctorProfileDTO profile = userProfileService.getDoctorProfile(userId);

        ApiResponse<DoctorProfileDTO> response = ApiResponse.success(
                "Doctor profile retrieved successfully",
                profile
        );
        return ResponseEntity.ok(response);
    }

    // ========================================================================
    // HELPER METHODS
    // ========================================================================

    /**
     * Gets the current authenticated user's ID from the security context
     */
    private String getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new RuntimeException("User is not authenticated");
        }
        return authentication.getName(); // Assuming the username is the UUID
    }

    /**
     * Safely gets the current authenticated user's ID, returns null if not authenticated
     */
    private String getCurrentUserIdSafe() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !authentication.isAuthenticated()
                    || "anonymousUser".equals(authentication.getName())) {
                return "system/admin";
            }
            return authentication.getName();
        } catch (Exception e) {
            return "system/admin";
        }
    }

    /**
     * Determines the target user ID based on request parameters and user permissions.
     * Admin users can operate on any user, while regular users can only operate on themselves.
     */
    private String determineTargetUserId(String targetUserId) {
        String currentUserId = getCurrentUserId();

        // If no targetUserId is specified, use the current user
        if (targetUserId == null || targetUserId.trim().isEmpty()) {
            return currentUserId;
        }

        // Check if current user is admin
        if (isCurrentUserAdmin()) {
            return targetUserId;
        }

        // If not admin, can only operate on self
        if (!currentUserId.equals(targetUserId)) {
            throw new SecurityException("Access denied: You can only manage your own profile");
        }

        return targetUserId;
    }

    /**
     * Checks if the current user has ADMIN role
     */
    private boolean isCurrentUserAdmin() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }

        return authentication.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals("ROLE_ADMIN")
                        || authority.getAuthority().equals("ADMIN"));
    }
}