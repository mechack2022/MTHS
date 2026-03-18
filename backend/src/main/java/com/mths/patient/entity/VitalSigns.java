package com.mths.patient.entity;

import com.mths.consultation.entity.Appointment;
import com.mths.auth.entity.User;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Entity
@Table(name = "vital_signs")
@Data
@EqualsAndHashCode(callSuper = false)
public class VitalSigns extends com.mths.shared.entity.BaseEntity {
    // Primary key inherited from BaseEntity as 'id'
    // Using 'id' field from BaseEntity instead of vitalId

    // Appointment ID is handled through relationship below
    // @Column(name = "appointment_id") - handled by @JoinColumn

    @Column(name = "blood_pressure")
    private String bloodPressure; // Format: "123/90"

    @Column(name = "heart_rate")
    private Integer heartRate;

    @Column(name = "temperature")
    private Double temperature;

    @Column(name = "weight")
    private Double weight;

    @Column(name = "height")
    private Double height;

    @Column(name = "bmi")
    private Double bmi;

    @Column(name = "recorded_at")
    private LocalDateTime recordedAt;

    // Who recorded these vitals (role-based)
    @Enumerated(EnumType.STRING)
    @Column(name = "recorded_by_role")
    private RecorderRole recordedByRole;

    // The actual user who created this record (for auditing)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recorded_by_user_id")
    private User recordedByUser;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    // Health assessment result
    @Enumerated(EnumType.STRING)
    @Column(name = "health_status")
    private HealthStatus healthStatus;

    // Relationships
    // Patient profile - REQUIRED (vitals always belong to a patient)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_profile_id", nullable = false)
    private PatientProfile patientProfile;

    // Appointment - OPTIONAL (vitals can be standalone or linked to appointment)
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "appointment_id", nullable = true)
    private Appointment appointment;

    @PrePersist
    protected void onCreate() {
        recordedAt = LocalDateTime.now();
        calculateBMI();
        assessHealthStatus();
    }

    @PreUpdate
    protected void onUpdate() {
        calculateBMI();
        assessHealthStatus();
    }

    // Calculate BMI automatically
    private void calculateBMI() {
        if (weight != null && height != null && height > 0) {
            double heightInMeters = height / 100.0;
            this.bmi = weight / (heightInMeters * heightInMeters);
            this.bmi = Math.round(this.bmi * 100.0) / 100.0;
        }
    }

    // Assess overall health status based on all vital signs
    private void assessHealthStatus() {
        int severeCount = 0;
        int moderateCount = 0;

        // Check blood pressure
        HealthStatus bpStatus = getBloodPressureStatus();
        if (bpStatus == HealthStatus.SEVERE) severeCount++;
        else if (bpStatus == HealthStatus.MODERATE) moderateCount++;

        // Check heart rate
        HealthStatus hrStatus = getHeartRateStatus();
        if (hrStatus == HealthStatus.SEVERE) severeCount++;
        else if (hrStatus == HealthStatus.MODERATE) moderateCount++;

        // Check temperature
        HealthStatus tempStatus = getTemperatureStatus();
        if (tempStatus == HealthStatus.SEVERE) severeCount++;
        else if (tempStatus == HealthStatus.MODERATE) moderateCount++;

        // Check BMI
        HealthStatus bmiStatus = getBMIStatus();
        if (bmiStatus == HealthStatus.SEVERE) severeCount++;
        else if (bmiStatus == HealthStatus.MODERATE) moderateCount++;

        // Determine overall status
        if (severeCount > 0) {
            this.healthStatus = HealthStatus.SEVERE;
        } else if (moderateCount > 0) {
            this.healthStatus = HealthStatus.MODERATE;
        } else {
            this.healthStatus = HealthStatus.MILD;
        }
    }

    // Blood pressure assessment
    private HealthStatus getBloodPressureStatus() {
        if (bloodPressure == null || bloodPressure.trim().isEmpty()) {
            return HealthStatus.MILD;
        }

        try {
            String[] parts = bloodPressure.split("/");
            if (parts.length != 2) return HealthStatus.MILD;

            int systolic = Integer.parseInt(parts[0].trim());
            int diastolic = Integer.parseInt(parts[1].trim());

            // Severe conditions (Orange)
            if (systolic >= 180 || diastolic >= 120 || systolic < 90 || diastolic < 60) {
                return HealthStatus.SEVERE;
            }
            // Moderate conditions (Yellow)
            else if (systolic >= 140 || diastolic >= 90 || systolic >= 130) {
                return HealthStatus.MODERATE;
            }
            // Normal/Mild (Green)
            else {
                return HealthStatus.MILD;
            }
        } catch (Exception e) {
            return HealthStatus.MILD;
        }
    }

    // Heart rate assessment
    private HealthStatus getHeartRateStatus() {
        if (heartRate == null) return HealthStatus.MILD;

        // Severe conditions (Orange)
        if (heartRate < 50 || heartRate > 120) {
            return HealthStatus.SEVERE;
        }
        // Moderate conditions (Yellow)
        else if (heartRate < 60 || heartRate > 100) {
            return HealthStatus.MODERATE;
        }
        // Normal/Mild (Green)
        else {
            return HealthStatus.MILD;
        }
    }

    // Temperature assessment
    private HealthStatus getTemperatureStatus() {
        if (temperature == null) return HealthStatus.MILD;

        // Severe conditions (Orange)
        if (temperature >= 39.0 || temperature <= 35.0) {
            return HealthStatus.SEVERE;
        }
        // Moderate conditions (Yellow)
        else if (temperature >= 37.5 || temperature <= 35.5) {
            return HealthStatus.MODERATE;
        }
        // Normal/Mild (Green)
        else {
            return HealthStatus.MILD;
        }
    }

    // BMI assessment
    private HealthStatus getBMIStatus() {
        if (bmi == null) return HealthStatus.MILD;

        // Severe conditions (Orange)
        if (bmi < 16 || bmi >= 35) {
            return HealthStatus.SEVERE;
        }
        // Moderate conditions (Yellow)
        else if (bmi < 18.5 || bmi >= 30) {
            return HealthStatus.MODERATE;
        }
        // Normal/Mild (Green)
        else {
            return HealthStatus.MILD;
        }
    }

    // Helper method to get systolic from blood pressure string
    public Integer getSystolicPressure() {
        if (bloodPressure == null || bloodPressure.trim().isEmpty()) {
            return null;
        }
        try {
            return Integer.parseInt(bloodPressure.split("/")[0].trim());
        } catch (Exception e) {
            return null;
        }
    }

    // Helper method to get diastolic from blood pressure string
    public Integer getDiastolicPressure() {
        if (bloodPressure == null || bloodPressure.trim().isEmpty()) {
            return null;
        }
        try {
            return Integer.parseInt(bloodPressure.split("/")[1].trim());
        } catch (Exception e) {
            return null;
        }
    }

    public enum HealthStatus {
        MILD("Mild Health Concerns", "green"),           // Green - No significant concerns
        MODERATE("Moderate Health Concerns", "yellow"),  // Yellow - Some concerns, monitoring needed
        SEVERE("Severe Condition", "orange");            // Orange - Immediate attention required

        private final String description;
        private final String color;

        HealthStatus(String description, String color) {
            this.description = description;
            this.color = color;
        }

        public String getDescription() {
            return description;
        }

        public String getColor() {
            return color;
        }
    }

    public enum RecorderRole {
        PATIENT("Patient Self-Reported"),          // Patient recorded their own vitals at home
        DOCTOR("Doctor Recorded"),                  // Doctor recorded during consultation
        NURSE("Nurse Recorded"),                    // Nurse recorded during triage/checkup
        LAB_TECHNICIAN("Lab Technician Recorded"),  // Lab tech during tests
        PHARMACIST("Pharmacist Recorded"),          // Pharmacist during consultation
        CAREGIVER("Caregiver Recorded"),           // Family member or caregiver
        SYSTEM("System Generated");                 // Automated/device reading

        private final String description;

        RecorderRole(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    // Helper method for appointment ID (delegate to relationship)
    public Long getAppointmentId() {
        return appointment != null ? appointment.getId() : null;
    }

    // Helper method for patient profile ID
    public Long getPatientProfileId() {
        return patientProfile != null ? patientProfile.getId() : null;
    }

    // Check if this is a standalone vital sign (not linked to appointment)
    public boolean isStandalone() {
        return appointment == null;
    }

    // Check if this is linked to an appointment
    public boolean isAppointmentLinked() {
        return appointment != null;
    }

    // Helper method for recorded by user ID
    public Long getRecordedByUserId() {
        return recordedByUser != null ? recordedByUser.getId() : null;
    }

    // Helper method to get recorder name
    public String getRecordedByName() {
        if (recordedByUser != null) {
            return recordedByUser.getFirstName() + " " + recordedByUser.getLastName();
        }
        return "Unknown";
    }
}