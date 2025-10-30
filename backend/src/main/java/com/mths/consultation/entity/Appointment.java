package com.mths.consultation.entity;

import com.mths.patient.entity.PatientProfile;
import com.mths.patient.entity.VitalSigns;
import com.mths.hospital.entity.DoctorProfile;
import com.mths.shared.constants.AppointmentStatus;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "appointments")
@Data
@EqualsAndHashCode(callSuper = false)
public class Appointment extends com.mths.shared.entity.BaseEntity {
    // Primary key inherited from BaseEntity as 'id'
    // Using 'id' field from BaseEntity instead of appointmentId

    // Profile IDs are handled through relationships below
    // @Column(name = "patient_profile_id") - handled by @JoinColumn
    // @Column(name = "doctor_profile_id") - handled by @JoinColumn

    @Enumerated(EnumType.STRING)
    @Column(name = "appointment_type")
    private AppointmentType appointmentType;

    @Column(name = "scheduled_datetime", nullable = false)
    private LocalDateTime scheduledDatetime;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private AppointmentStatus status = AppointmentStatus.SCHEDULED;

    @Column(name = "consultation_notes", columnDefinition = "TEXT")
    private String consultationNotes;

    @Column(name = "symptoms", columnDefinition = "TEXT")
    private String symptoms;

    @Column(name = "diagnosis", columnDefinition = "TEXT")
    private String diagnosis;

    @Column(name = "consultation_fee")
    private Double consultationFee;

    @Column(name = "duration_minutes")
    private Integer durationMinutes = 30; // Default 30 minutes

    @Column(name = "meeting_url")
    private String meetingUrl; // For virtual appointments

    @Column(name = "meeting_id")
    private String meetingId;

    @Column(name = "cancelled_reason")
    private String cancelledReason;

    @Column(name = "cancelled_by")
    private String cancelledBy; // patient, doctor, system

    @Column(name = "cancelled_at")
    private LocalDateTime cancelledAt;

    // Relationships
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_profile_id")
    private PatientProfile patientProfile;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "doctor_profile_id")
    private DoctorProfile doctorProfile;

    @OneToMany(mappedBy = "appointment", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<AppointmentReschedule> reschedules = new ArrayList<>();

    @OneToOne(mappedBy = "appointment", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private VitalSigns vitalSigns;

    // TODO: Add when Prescription and LabOrder entities are created
    // @OneToMany(mappedBy = "appointment", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    // private List<Prescription> prescriptions = new ArrayList<>();

    // @OneToMany(mappedBy = "appointment", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    // private List<LabOrder> labOrders = new ArrayList<>();

    public enum AppointmentType {
        VIRTUAL("Virtual"),
        IN_PERSON("In Person");

        private final String description;

        AppointmentType(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    // Helper methods for profile IDs (delegate to relationships)
    public Long getPatientProfileId() {
        return patientProfile != null ? patientProfile.getId() : null;
    }
    
    public Long getDoctorProfileId() {
        return doctorProfile != null ? doctorProfile.getId() : null;
    }

    // Helper methods for status
    public boolean isScheduled() {
        return status == AppointmentStatus.SCHEDULED;
    }

    public boolean isConfirmed() {
        return status == AppointmentStatus.CONFIRMED;
    }

    public boolean isCompleted() {
        return status == AppointmentStatus.COMPLETED;
    }

    public boolean isCancelled() {
        return status == AppointmentStatus.CANCELLED;
    }

    public boolean canBeRescheduled() {
        return status == AppointmentStatus.SCHEDULED || status == AppointmentStatus.CONFIRMED;
    }

    public boolean canBeCancelled() {
        return status != AppointmentStatus.COMPLETED && status != AppointmentStatus.CANCELLED;
    }
}