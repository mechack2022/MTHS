package com.mths.prescription.mapper;

import com.mths.prescription.dto.PrescriptionResponse;
import com.mths.prescription.entity.Prescription;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Mapper for converting Prescription entities to DTOs
 */
@Component
@RequiredArgsConstructor
public class PrescriptionMapper {

    public PrescriptionResponse toResponse(Prescription prescription) {
        if (prescription == null) {
            return null;
        }

        PrescriptionResponse response = new PrescriptionResponse();
        response.setId(prescription.getId());
        response.setPrescriptionNumber(prescription.getPrescriptionNumber());

        // Patient information
        if (prescription.getPatientProfile() != null) {
            response.setPatientProfileId(prescription.getPatientProfile().getId());
            response.setPatientName(prescription.getPatientProfile().getFullName());
        }

        // Doctor information
        if (prescription.getDoctorProfile() != null) {
            response.setDoctorProfileId(prescription.getDoctorProfile().getId());
            response.setDoctorName(prescription.getDoctorProfile().getFullName());
        }

        // Appointment information
        if (prescription.getAppointment() != null) {
            response.setAppointmentId(prescription.getAppointment().getId());
        }

        // Pharmacy information
        if (prescription.getPharmacy() != null) {
            response.setPharmacyId(prescription.getPharmacy().getId());
            response.setPharmacyName(prescription.getPharmacy().getPharmacyName());
        }

        // Pharmacist information
        if (prescription.getPharmacist() != null) {
            response.setPharmacistId(prescription.getPharmacist().getId());
            response.setPharmacistName(prescription.getPharmacist().getFullName());
        }

        // Prescription details
        response.setCreationContext(prescription.getCreationContext());
        response.setDiagnosis(prescription.getDiagnosis());
        response.setNotes(prescription.getNotes());
        response.setIssuedDate(prescription.getIssuedDate());
        response.setValidUntil(prescription.getValidUntil());
        response.setStatus(prescription.getStatus());
        response.setPharmacyStatus(prescription.getPharmacyStatus());

        // Dispensing information
        response.setDispensedDate(prescription.getDispensedDate());
        response.setDispensedBy(prescription.getDispensedBy());
        response.setSentToPharmacyAt(prescription.getSentToPharmacyAt());
        response.setPharmacyNotes(prescription.getPharmacyNotes());

        // Refill information
        response.setIsRefillable(prescription.getIsRefillable());
        response.setRefillCount(prescription.getRefillCount());
        response.setRefillsRemaining(prescription.getRefillsRemaining());
        response.setIsOngoingTreatment(prescription.getIsOngoingTreatment());

        // Previous prescription
        if (prescription.getPreviousPrescription() != null) {
            response.setPreviousPrescriptionId(prescription.getPreviousPrescription().getId());
        }

        // Document information
        response.setPrescriptionDocumentPath(prescription.getPrescriptionDocumentPath());
        response.setPrescriptionDocumentName(prescription.getPrescriptionDocumentName());
        response.setPrescriptionDocumentType(prescription.getPrescriptionDocumentType());
        response.setPrescriptionDocumentSize(prescription.getPrescriptionDocumentSize());
        response.setPrescriptionDocumentUploadedAt(prescription.getPrescriptionDocumentUploadedAt());

        // Helper/computed fields
        response.setIsExpired(prescription.isExpired());
        response.setCanBeDispensed(prescription.canBeDispensed());
        response.setCanBeRefilled(prescription.canBeRefilled());
        response.setIsSentToPharmacy(prescription.isSentToPharmacy());
        response.setIsReadyForPickup(prescription.isReadyForPickup());
        response.setHasDocument(prescription.hasDocument());

        // Audit fields
        response.setCreatedAt(prescription.getCreatedAt());
        response.setUpdatedAt(prescription.getUpdatedAt());
        response.setCreatedBy(prescription.getCreatedBy());

        return response;
    }
}
