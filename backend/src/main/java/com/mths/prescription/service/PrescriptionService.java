package com.mths.prescription.service;

import com.mths.prescription.dto.CreatePrescriptionRequest;
import com.mths.prescription.dto.DispenseMedicationRequest;
import com.mths.prescription.dto.PrescriptionResponse;
import com.mths.prescription.dto.SendToPharmacyRequest;
import com.mths.prescription.entity.Prescription;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface PrescriptionService {

    PrescriptionResponse createPrescription(CreatePrescriptionRequest request, MultipartFile document);

    PrescriptionResponse getPrescriptionById(Long prescriptionId);

    PrescriptionResponse getPrescriptionByNumber(String prescriptionNumber);

    PrescriptionResponse updatePrescriptionDocument(Long prescriptionId, MultipartFile document);

    List<PrescriptionResponse> getPatientPrescriptions(Long patientProfileId);

    List<PrescriptionResponse> getActivePatientPrescriptions(Long patientProfileId);

    List<PrescriptionResponse> getDoctorPrescriptions(Long doctorProfileId);

    /**
     * Get prescriptions for an appointment
     *
     * @param appointmentId Appointment ID
     * @return List of prescriptions
     */
    List<PrescriptionResponse> getAppointmentPrescriptions(Long appointmentId);

    /**
     * Send prescription to pharmacy for fulfillment
     *
     * @param prescriptionId Prescription ID
     * @param request Send to pharmacy request
     * @return Updated prescription response
     */
    PrescriptionResponse sendToPharmacy(Long prescriptionId, SendToPharmacyRequest request);

    /**
     * Get prescriptions for a pharmacy
     *
     * @param pharmacyId Pharmacy ID
     * @return List of prescriptions
     */
    List<PrescriptionResponse> getPharmacyPrescriptions(Long pharmacyId);

    /**
     * Get prescriptions by pharmacy status
     *
     * @param pharmacyId Pharmacy ID
     * @param status Pharmacy status
     * @return List of prescriptions
     */
    List<PrescriptionResponse> getPharmacyPrescriptionsByStatus(Long pharmacyId, Prescription.PharmacyStatus status);

    /**
     * Update pharmacy status
     *
     * @param prescriptionId Prescription ID
     * @param status New pharmacy status
     * @param pharmacyNotes Optional notes from pharmacist
     * @return Updated prescription response
     */
    PrescriptionResponse updatePharmacyStatus(Long prescriptionId, Prescription.PharmacyStatus status, String pharmacyNotes);

    /**
     * Dispense medication to patient
     *
     * @param prescriptionId Prescription ID
     * @param request Dispense medication request
     * @return Updated prescription response
     */
    PrescriptionResponse dispenseMedication(Long prescriptionId, DispenseMedicationRequest request);

    /**
     * Get refillable prescriptions for a patient
     *
     * @param patientProfileId Patient profile ID
     * @return List of refillable prescriptions
     */
    List<PrescriptionResponse> getRefillablePrescriptions(Long patientProfileId);

    /**
     * Create a refill prescription
     *
     * @param originalPrescriptionId Original prescription ID
     * @param document New prescription document
     * @return Created refill prescription
     */
    PrescriptionResponse createRefill(Long originalPrescriptionId, MultipartFile document);

    /**
     * Cancel prescription
     *
     * @param prescriptionId Prescription ID
     * @param reason Cancellation reason
     * @return Updated prescription response
     */
    PrescriptionResponse cancelPrescription(Long prescriptionId, String reason);

    /**
     * Mark prescription as expired
     *
     * @param prescriptionId Prescription ID
     * @return Updated prescription response
     */
    PrescriptionResponse expirePrescription(Long prescriptionId);

    /**
     * Download prescription document
     *
     * @param prescriptionId Prescription ID
     * @return Document file path for download
     */
    String getPrescriptionDocumentPath(Long prescriptionId);


    List<PrescriptionResponse> getPrescriptionsReadyForPickup();

    List<PrescriptionResponse> getPendingPharmacyPrescriptions();

    int updateExpiredPrescriptions();
}
