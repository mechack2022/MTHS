package com.mths.prescription.entity;

import com.mths.shared.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * PrescriptionItem Entity
 * Represents individual medication in a prescription
 * One prescription can have multiple items (medications)
 */
@Entity
@Table(name = "prescription_items")
@Data
@EqualsAndHashCode(callSuper = false)
public class PrescriptionItem extends BaseEntity {

    // Parent prescription
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "prescription_id", nullable = false)
    private Prescription prescription;

    // Medication details
    @Column(name = "medication_name", nullable = false)
    private String medicationName; // e.g., "Amoxicillin 500mg"

    @Column(name = "generic_name")
    private String genericName; // Generic/chemical name

    @Column(name = "dosage", nullable = false)
    private String dosage; // e.g., "500mg", "10ml"

    @Column(name = "frequency", nullable = false)
    private String frequency; // e.g., "Twice daily", "Every 8 hours", "3 times per day"

    @Column(name = "duration", nullable = false)
    private String duration; // e.g., "7 days", "2 weeks", "1 month"

    @Column(name = "route")
    private String route; // e.g., "Oral", "Topical", "Injection"

    @Column(name = "quantity", nullable = false)
    private Integer quantity; // Number of pills/bottles/units to dispense

    @Column(name = "unit")
    private String unit; // e.g., "tablets", "capsules", "ml", "bottles"

    // Instructions
    @Column(name = "instructions", columnDefinition = "TEXT")
    private String instructions; // e.g., "Take with food", "Take before bed"

    @Column(name = "special_instructions", columnDefinition = "TEXT")
    private String specialInstructions; // Warnings, side effects to watch for

    // Timing
    @Enumerated(EnumType.STRING)
    @Column(name = "timing")
    private MedicationTiming timing;

    // Status
    @Column(name = "is_dispensed")
    private Boolean isDispensed = false;

    @Column(name = "quantity_dispensed")
    private Integer quantityDispensed;

    // Medication timing enum
    public enum MedicationTiming {
        BEFORE_MEAL("Take before meals"),
        AFTER_MEAL("Take after meals"),
        WITH_MEAL("Take with meals"),
        EMPTY_STOMACH("Take on empty stomach"),
        BEDTIME("Take at bedtime"),
        AS_NEEDED("Take as needed"),
        MORNING("Take in the morning"),
        EVENING("Take in the evening");

        private final String description;

        MedicationTiming(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    // Helper methods
    public boolean isFullyDispensed() {
        return Boolean.TRUE.equals(isDispensed) &&
               quantityDispensed != null &&
               quantityDispensed.equals(quantity);
    }

    public Integer getRemainingQuantity() {
        if (quantityDispensed == null) {
            return quantity;
        }
        return quantity - quantityDispensed;
    }
}
