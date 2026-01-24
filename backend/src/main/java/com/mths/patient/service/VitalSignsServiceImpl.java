package com.mths.patient.service;

import com.mths.auth.entity.User;
import com.mths.auth.repository.UserRepository;
import com.mths.patient.dto.CreateVitalSignsRequest;
import com.mths.patient.dto.VitalSignsDTO;
import com.mths.patient.entity.PatientProfile;
import com.mths.patient.entity.VitalSigns;
import com.mths.patient.repository.PatientProfileRepository;
import com.mths.patient.repository.VitalSignsRepository;
import com.mths.shared.exceptions.ResourceNotFoundException;
import com.mths.shared.mapper.AppointmentMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service implementation for managing standalone vital signs
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class VitalSignsServiceImpl implements VitalSignsService {

    private final VitalSignsRepository vitalSignsRepository;
    private final PatientProfileRepository patientProfileRepository;
    private final UserRepository userRepository;
    private final AppointmentMapper appointmentMapper;

    @Override
    public VitalSignsDTO createVitalSigns(CreateVitalSignsRequest request) {
        log.info("Creating standalone vital signs for patient: {}", request.getPatientProfileId());

        // Get patient profile
        PatientProfile patientProfile = patientProfileRepository.findById(request.getPatientProfileId())
                .orElseThrow(() -> new ResourceNotFoundException("PatientProfile", "id", request.getPatientProfileId()));

        // Create vital signs entity
        VitalSigns vitalSigns = new VitalSigns();
        vitalSigns.setPatientProfile(patientProfile);
        vitalSigns.setBloodPressure(request.getBloodPressure());
        vitalSigns.setHeartRate(request.getHeartRate());
        vitalSigns.setTemperature(request.getTemperature());
        vitalSigns.setWeight(request.getWeight());
        vitalSigns.setHeight(request.getHeight());
        vitalSigns.setNotes(request.getNotes());

        // Set recorder information from authenticated user
        setRecorderInformation(vitalSigns);

        // Note: appointment is NULL for standalone vitals
        // BMI and health status are auto-calculated in @PrePersist

        VitalSigns savedVitalSigns = vitalSignsRepository.save(vitalSigns);

        log.info("Standalone vital signs created - ID: {}, Health Status: {}, Recorded by: {}",
                savedVitalSigns.getId(),
                savedVitalSigns.getHealthStatus(),
                savedVitalSigns.getRecordedByRole());

        return appointmentMapper.mapToVitalSignsDTO(savedVitalSigns);
    }

    @Override
    @Transactional(readOnly = true)
    public VitalSignsDTO getVitalSignsById(Long vitalSignsId) {
        VitalSigns vitalSigns = vitalSignsRepository.findById(vitalSignsId)
                .orElseThrow(() -> new ResourceNotFoundException("VitalSigns", "id", vitalSignsId));

        return appointmentMapper.mapToVitalSignsDTO(vitalSigns);
    }

    @Override
    public VitalSignsDTO updateVitalSigns(Long vitalSignsId, VitalSignsDTO vitalSignsDTO) {
        log.info("Updating vital signs: {}", vitalSignsId);

        VitalSigns vitalSigns = vitalSignsRepository.findById(vitalSignsId)
                .orElseThrow(() -> new ResourceNotFoundException("VitalSigns", "id", vitalSignsId));

        // Update fields
        if (vitalSignsDTO.getBloodPressure() != null) {
            vitalSigns.setBloodPressure(vitalSignsDTO.getBloodPressure());
        }
        if (vitalSignsDTO.getHeartRate() != null) {
            vitalSigns.setHeartRate(vitalSignsDTO.getHeartRate());
        }
        if (vitalSignsDTO.getTemperature() != null) {
            vitalSigns.setTemperature(vitalSignsDTO.getTemperature());
        }
        if (vitalSignsDTO.getWeight() != null) {
            vitalSigns.setWeight(vitalSignsDTO.getWeight());
        }
        if (vitalSignsDTO.getHeight() != null) {
            vitalSigns.setHeight(vitalSignsDTO.getHeight());
        }
        if (vitalSignsDTO.getNotes() != null) {
            vitalSigns.setNotes(vitalSignsDTO.getNotes());
        }

        // BMI and health status auto-recalculated in @PreUpdate

        VitalSigns updatedVitalSigns = vitalSignsRepository.save(vitalSigns);

        log.info("Vital signs updated - ID: {}, Health Status: {}",
                updatedVitalSigns.getId(), updatedVitalSigns.getHealthStatus());

        return appointmentMapper.mapToVitalSignsDTO(updatedVitalSigns);
    }

    @Override
    public void deleteVitalSigns(Long vitalSignsId) {
        log.info("Deleting vital signs: {}", vitalSignsId);

        VitalSigns vitalSigns = vitalSignsRepository.findById(vitalSignsId)
                .orElseThrow(() -> new ResourceNotFoundException("VitalSigns", "id", vitalSignsId));

        vitalSignsRepository.delete(vitalSigns);

        log.info("Vital signs deleted: {}", vitalSignsId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<VitalSignsDTO> getPatientVitalSignsHistory(Long patientProfileId) {
        log.info("Retrieving vital signs history for patient: {}", patientProfileId);

        List<VitalSigns> vitalSignsList = vitalSignsRepository.findByPatientProfileId(patientProfileId);

        return vitalSignsList.stream()
                .map(appointmentMapper::mapToVitalSignsDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<VitalSignsDTO> getPatientStandaloneVitalSigns(Long patientProfileId) {
        log.info("Retrieving standalone vital signs for patient: {}", patientProfileId);

        List<VitalSigns> vitalSignsList = vitalSignsRepository.findStandaloneVitalSignsByPatientId(patientProfileId);

        return vitalSignsList.stream()
                .map(appointmentMapper::mapToVitalSignsDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<VitalSignsDTO> getPatientsWithSevereVitals() {
        log.info("Retrieving patients with severe vital signs");

        List<VitalSigns> severeVitalsList = vitalSignsRepository.findSevereVitalSigns();

        return severeVitalsList.stream()
                .map(appointmentMapper::mapToVitalSignsDTO)
                .collect(Collectors.toList());
    }

    // ========================================================================
    // PRIVATE HELPER METHODS
    // ========================================================================

    /**
     * Set recorder information from authenticated user
     * Automatically determines role and captures user details
     */
    private void setRecorderInformation(VitalSigns vitalSigns) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null && authentication.isAuthenticated()) {
            String userEmail = authentication.getName(); // Returns email/username

            // Get user from database by email
            User user = userRepository.findByEmail(userEmail).orElse(null);

            if (user != null) {
                vitalSigns.setRecordedByUser(user);

                // Determine recorder role from user's account type
                VitalSigns.RecorderRole recorderRole = determineRecorderRole(user);
                vitalSigns.setRecordedByRole(recorderRole);

                log.debug("Set recorder: {} ({})", user.getEmail(), recorderRole);
            } else {
                // Fallback if user not found
                vitalSigns.setRecordedByRole(VitalSigns.RecorderRole.SYSTEM);
                log.warn("Could not find user for email: {}, setting recorder role to SYSTEM", userEmail);
            }
        } else {
            // Fallback for unauthenticated requests (shouldn't happen with security enabled)
            vitalSigns.setRecordedByRole(VitalSigns.RecorderRole.SYSTEM);
            log.warn("No authentication found, setting recorder role to SYSTEM");
        }
    }

    /**
     * Determine recorder role based on user's account type
     */
    private VitalSigns.RecorderRole determineRecorderRole(User user) {
        return switch (user.getAccountType()) {
            case PATIENT -> VitalSigns.RecorderRole.PATIENT;
            case DOCTOR -> VitalSigns.RecorderRole.DOCTOR;
            case LAB_TECHNICIAN -> VitalSigns.RecorderRole.LAB_TECHNICIAN;
            case PHARMACIST -> VitalSigns.RecorderRole.PHARMACIST;
            default -> VitalSigns.RecorderRole.SYSTEM;
        };
    }
}
