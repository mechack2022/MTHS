package com.mths.pharmacy.entity;

import com.mths.shared.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalTime;

/**
 * Pharmacy Entity
 * Represents a registered pharmacy that can fulfill prescriptions
 */
@Entity
@Table(name = "pharmacies")
@Data
@EqualsAndHashCode(callSuper = false)
public class Pharmacy extends BaseEntity {

    @Column(name = "pharmacy_name", nullable = false)
    private String pharmacyName;

    @Column(name = "license_number", unique = true, nullable = false)
    private String licenseNumber;

    @Column(name = "registration_number", unique = true)
    private String registrationNumber; // Pharmacy council registration

    // Contact Information
    @Column(name = "email", unique = true, nullable = false)
    private String email;

    @Column(name = "phone_number", nullable = false)
    private String phoneNumber;

    @Column(name = "alternative_phone")
    private String alternativePhone;

    // Location
    @Column(name = "address", nullable = false, columnDefinition = "TEXT")
    private String address;

    @Column(name = "city", nullable = false)
    private String city;

    @Column(name = "state", nullable = false)
    private String state;

    @Column(name = "postal_code")
    private String postalCode;

    @Column(name = "country", nullable = false)
    private String country = "Nigeria";

    @Column(name = "latitude")
    private Double latitude; // For location-based search

    @Column(name = "longitude")
    private Double longitude;

    // Operating Hours
    @Column(name = "opening_time")
    private LocalTime openingTime;

    @Column(name = "closing_time")
    private LocalTime closingTime;

    @Column(name = "is_24_hours")
    private Boolean is24Hours = false;

    @Column(name = "operating_days")
    private String operatingDays; // e.g., "Mon-Fri", "Mon-Sat", "All days"

    // Pharmacy Details
    @Column(name = "owner_name")
    private String ownerName; // Pharmacy owner/superintendent

    @Column(name = "superintendent_pharmacist")
    private String superintendentPharmacist; // Licensed pharmacist in charge

    @Column(name = "services_offered", columnDefinition = "TEXT")
    private String servicesOffered; // e.g., "Prescription filling, OTC medications, Consultations"

    @Column(name = "accepts_insurance")
    private Boolean acceptsInsurance = false;

    @Column(name = "insurance_providers", columnDefinition = "TEXT")
    private String insuranceProviders; // Comma-separated list

    // Pharmacy Status
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private PharmacyStatus status = PharmacyStatus.PENDING_VERIFICATION;

    @Column(name = "verification_date")
    private java.time.LocalDateTime verificationDate;

    @Column(name = "verified_by")
    private String verifiedBy; // Admin who verified the pharmacy

    @Column(name = "is_active")
    private Boolean isActive = true;

    @Column(name = "deactivation_reason", columnDefinition = "TEXT")
    private String deactivationReason;

    // Pharmacy Rating & Reviews
    @Column(name = "rating")
    private Double rating = 0.0; // Average rating (0-5)

    @Column(name = "total_reviews")
    private Integer totalReviews = 0;

    @Column(name = "total_prescriptions_filled")
    private Integer totalPrescriptionsFilled = 0;

    // Notes
    @Column(name = "admin_notes", columnDefinition = "TEXT")
    private String adminNotes; // Internal notes for administrators

    @Column(name = "public_notes", columnDefinition = "TEXT")
    private String publicNotes; // Visible to patients (e.g., "Located near General Hospital")

    // Pharmacy Status Enum
    public enum PharmacyStatus {
        PENDING_VERIFICATION("Registration pending verification"),
        VERIFIED("Verified and active"),
        SUSPENDED("Temporarily suspended"),
        DEACTIVATED("Permanently deactivated");

        private final String description;

        PharmacyStatus(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    // Helper methods
    public boolean isVerified() {
        return status == PharmacyStatus.VERIFIED;
    }

    public boolean canAcceptPrescriptions() {
        return Boolean.TRUE.equals(isActive) && status == PharmacyStatus.VERIFIED;
    }

    public boolean isCurrentlyOpen() {
        if (Boolean.TRUE.equals(is24Hours)) {
            return true;
        }

        if (openingTime == null || closingTime == null) {
            return false;
        }

        LocalTime now = LocalTime.now();
        return !now.isBefore(openingTime) && !now.isAfter(closingTime);
    }

    public String getFullAddress() {
        return String.format("%s, %s, %s %s, %s",
                address, city, state,
                postalCode != null ? postalCode : "",
                country).trim();
    }

    public void incrementPrescriptionCount() {
        this.totalPrescriptionsFilled = (this.totalPrescriptionsFilled != null ? this.totalPrescriptionsFilled : 0) + 1;
    }

    public void updateRating(double newRating) {
        if (this.totalReviews == null) {
            this.totalReviews = 0;
            this.rating = 0.0;
        }

        // Calculate new average rating
        double totalRating = (this.rating * this.totalReviews) + newRating;
        this.totalReviews++;
        this.rating = totalRating / this.totalReviews;
    }
}
