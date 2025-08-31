package com.auth.service.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Entity
@Table(name = "vital_signs")
@Data
@EqualsAndHashCode(callSuper = false)
public class VitalSigns extends BaseEntity {
    // Primary key inherited from BaseEntity as 'id'
    // Using 'id' field from BaseEntity instead of vitalId

    @Column(name = "appointment_id", nullable = false, insertable = false, updatable = false)
    private Long appointmentId;

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

    @Column(name = "recorded_by")
    private String recordedBy;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    // Health assessment result
    @Enumerated(EnumType.STRING)
    @Column(name = "health_status")
    private HealthStatus healthStatus;

    // Relationships
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "appointment_id")
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
}