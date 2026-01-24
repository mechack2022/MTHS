package com.mths.patient.service;

import com.mths.patient.dto.CreateVitalSignsRequest;
import com.mths.patient.dto.VitalSignsDTO;

import java.util.List;

/**
 * Service for managing standalone vital signs
 * (vitals not linked to appointments)
 */
public interface VitalSignsService {

    /**x
     * Create standalone vital signs for a patient
     * Used for home monitoring, walk-in checks, etc.
     */
    VitalSignsDTO createVitalSigns(CreateVitalSignsRequest request);

    /**
     * Get a vital signs record by ID
     */
    VitalSignsDTO getVitalSignsById(Long vitalSignsId);

    /**
     * Update existing vital signs
     */
    VitalSignsDTO updateVitalSigns(Long vitalSignsId, VitalSignsDTO vitalSignsDTO);

    /**
     * Delete vital signs
     */
    void deleteVitalSigns(Long vitalSignsId);

    /**
     * Get all vital signs for a patient (both standalone and appointment-linked)
     */
    List<VitalSignsDTO> getPatientVitalSignsHistory(Long patientProfileId);

    /**
     * Get only standalone vital signs for a patient (not linked to appointments)
     */
    List<VitalSignsDTO> getPatientStandaloneVitalSigns(Long patientProfileId);

    /**
     * Get recent vital signs with severe health status
     */
    List<VitalSignsDTO> getPatientsWithSevereVitals();
}
