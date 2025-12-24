package com.mths.prescription.entity;

import com.mths.consultation.entity.Appointment;
import com.mths.hospital.entity.DoctorProfile;
import com.mths.patient.entity.PatientProfile;
import com.mths.pharmacy.entity.Pharmacy;
import com.mths.pharmacy.entity.Pharmacist;
import com.mths.shared.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Prescription Entity
 * Supports two creation contexts:
 * 1. During Consultation: Linked to appointment
 * 2. Via Records: Standalone prescription from patient records
 */
@Entity
@Table(name = "prescriptions")
@Data
@EqualsAndHashCode(callSuper = false)
public class Prescription extends BaseEntity {

    // Reference number for prescription
    @Column(name = "prescription_number", unique = true, nullable = false)
    private String prescriptionNumber; // Format: RX-{patientId}-{timestamp}

    // REQUIRED: Patient who receives prescription
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_profile_id", nullable = false)
    private PatientProfile patientProfile;

    // REQUIRED: Doctor who issued prescription
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "doctor_profile_id", nullable = false)
    private DoctorProfile doctorProfile;

    // OPTIONAL: Appointment (null if created via records)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "appointment_id")
    private Appointment appointment;

    // Creation context
    @Enumerated(EnumType.STRING)
    @Column(name = "creation_context", nullable = false)
    private CreationContext creationContext;

    // Prescription details
    @Column(name = "diagnosis", columnDefinition = "TEXT")
    private String diagnosis; // Medical condition being treated

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes; // Additional instructions or notes

    // Prescription dates
    @Column(name = "issued_date", nullable = false)
    private LocalDate issuedDate;

    @Column(name = "valid_until")
    private LocalDate validUntil; // Expiration date

    // Status tracking
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private PrescriptionStatus status = PrescriptionStatus.ACTIVE;

    @Column(name = "dispensed_date")
    private LocalDateTime dispensedDate; // When medication was given to patient

    @Column(name = "dispensed_by")
    private String dispensedBy; // Pharmacist/nurse who dispensed

    // Pharmacy workflow
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pharmacy_id")
    private Pharmacy pharmacy; // Pharmacy selected/recommended to fulfill prescription

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pharmacist_id")
    private Pharmacist pharmacist; // Pharmacist who dispensed the medication

    @Column(name = "sent_to_pharmacy_at")
    private LocalDateTime sentToPharmacyAt; // When prescription was sent to pharmacy

    @Enumerated(EnumType.STRING)
    @Column(name = "pharmacy_status")
    private PharmacyStatus pharmacyStatus = PharmacyStatus.NOT_SENT;

    @Column(name = "pharmacy_notes", columnDefinition = "TEXT")
    private String pharmacyNotes; // Notes from pharmacist during fulfillment

    // Refill information
    @Column(name = "is_refillable")
    private Boolean isRefillable = false;

    @Column(name = "refill_count")
    private Integer refillCount = 0; // Number of refills allowed

    @Column(name = "refills_remaining")
    private Integer refillsRemaining = 0;

    // Medications (One prescription can have multiple medications)
    @OneToMany(mappedBy = "prescription", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PrescriptionItem> prescriptionItems = new ArrayList<>();

    // For prescriptions created via records - link to previous prescription if it's a refill
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "previous_prescription_id")
    private Prescription previousPrescription;

    // Track if this is part of ongoing treatment
    @Column(name = "is_ongoing_treatment")
    private Boolean isOngoingTreatment = false;

    // Creation context enum
    public enum CreationContext {
        DURING_CONSULTATION("Created during patient consultation"),
        FROM_RECORDS("Created from patient medical records"),
        REFILL("Refill of previous prescription"),
        EMERGENCY("Emergency prescription");

        private final String description;

        CreationContext(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    // Status enum
    public enum PrescriptionStatus {
        ACTIVE("Currently active prescription"),
        DISPENSED("Medication dispensed to patient"),
        COMPLETED("Treatment completed"),
        EXPIRED("Prescription expired"),
        CANCELLED("Prescription cancelled"),
        SUPERSEDED("Replaced by newer prescription");

        private final String description;

        PrescriptionStatus(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    // Pharmacy status enum
    public enum PharmacyStatus {
        NOT_SENT("Prescription not yet sent to pharmacy"),
        PENDING("Sent to pharmacy, awaiting fulfillment"),
        IN_PROGRESS("Pharmacist preparing medication"),
        READY_FOR_PICKUP("Medication ready for patient pickup"),
        DISPENSED("Medication dispensed to patient"),
        CANCELLED("Pharmacy request cancelled");

        private final String description;

        PharmacyStatus(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    // Helper methods
    public boolean isFromConsultation() {
        return creationContext == CreationContext.DURING_CONSULTATION;
    }

    public boolean isFromRecords() {
        return creationContext == CreationContext.FROM_RECORDS ||
               creationContext == CreationContext.REFILL;
    }

    public boolean isExpired() {
        return validUntil != null && LocalDate.now().isAfter(validUntil);
    }

    public boolean canBeDispensed() {
        return status == PrescriptionStatus.ACTIVE && !isExpired();
    }

    public boolean canBeRefilled() {
        return isRefillable && refillsRemaining != null && refillsRemaining > 0;
    }

    public void addPrescriptionItem(PrescriptionItem item) {
        prescriptionItems.add(item);
        item.setPrescription(this);
    }

    public void removePrescriptionItem(PrescriptionItem item) {
        prescriptionItems.remove(item);
        item.setPrescription(null);
    }

    public boolean isSentToPharmacy() {
        return pharmacyStatus != PharmacyStatus.NOT_SENT;
    }

    public boolean canBeSentToPharmacy() {
        return status == PrescriptionStatus.ACTIVE &&
               !isExpired() &&
               pharmacyStatus == PharmacyStatus.NOT_SENT;
    }

    public boolean isReadyForPickup() {
        return pharmacyStatus == PharmacyStatus.READY_FOR_PICKUP;
    }

    public void sendToPharmacy(Pharmacy selectedPharmacy) {
        this.pharmacy = selectedPharmacy;
        this.pharmacyStatus = PharmacyStatus.PENDING;
        this.sentToPharmacyAt = LocalDateTime.now();
    }

    @PrePersist
    protected void onCreate() {
        if (issuedDate == null) {
            issuedDate = LocalDate.now();
        }

        // Auto-set valid until date (default 30 days for regular prescriptions)
        if (validUntil == null && creationContext == CreationContext.DURING_CONSULTATION) {
            validUntil = issuedDate.plusDays(30);
        }

        // For ongoing treatment, set longer validity
        if (Boolean.TRUE.equals(isOngoingTreatment) && validUntil == null) {
            validUntil = issuedDate.plusMonths(6);
        }
    }

    @PreUpdate
    protected void onUpdate() {
        // Auto-expire if past valid date
        if (isExpired() && status == PrescriptionStatus.ACTIVE) {
            status = PrescriptionStatus.EXPIRED;
        }
    }
}
