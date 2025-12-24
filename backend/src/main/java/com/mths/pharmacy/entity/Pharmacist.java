package com.mths.pharmacy.entity;

import com.mths.auth.entity.UserProfile;
import com.mths.shared.constants.Gender;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

/**
 * Pharmacist Profile Entity
 * Represents a licensed pharmacist who works at a pharmacy
 * Pharmacists can dispense medications and manage prescriptions
 * Follows the UserProfile inheritance pattern for consistency
 */
@Entity
@Table(name = "pharmacist_profiles")
@DiscriminatorValue("PHARMACIST")
@Setter
@Getter
public class Pharmacist extends UserProfile {

    // Pharmacy they work at
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pharmacy_id")
    private Pharmacy pharmacy;

    // Professional Information
    @Column(name = "license_number", unique = true, nullable = false)
    private String licenseNumber; // Pharmacist council license number

    @Column(name = "registration_number", unique = true)
    private String registrationNumber; // Professional registration number

    @Column(name = "license_expiry_date")
    private LocalDate licenseExpiryDate;

    @Column(name = "issuing_authority")
    private String issuingAuthority; // e.g., "Pharmacists Council of Nigeria"

    // Personal Details
    @Enumerated(EnumType.STRING)
    @Column(name = "gender")
    private Gender gender;

    @Column(name = "date_of_birth")
    private LocalDate dateOfBirth;

    // Contact Information (phone and address via UserProfile overrides)
    @Column(name = "phone_number", unique = true)
    private String phoneNumber;

    @Column(name = "alternative_phone")
    private String alternativePhone;

    @Column(name = "address", columnDefinition = "TEXT")
    private String address;

    // Professional Details
    @Column(name = "specialization")
    private String specialization; // e.g., "Clinical Pharmacy", "Hospital Pharmacy", "Community Pharmacy"

    @Column(name = "years_of_experience")
    private Integer yearsOfExperience;

    @Column(name = "qualifications", columnDefinition = "TEXT")
    private String qualifications; // Comma-separated list: "B.Pharm, M.Sc. Clinical Pharmacy"

    @Column(name = "pharmacy_school")
    private String pharmacySchool; // University/institution

    @Column(name = "graduation_year")
    private Integer graduationYear;

    // Employment Details
    @Enumerated(EnumType.STRING)
    @Column(name = "employment_type")
    private EmploymentType employmentType = EmploymentType.FULL_TIME;

    @Column(name = "position")
    private String position; // e.g., "Chief Pharmacist", "Staff Pharmacist", "Intern Pharmacist"

    @Column(name = "hire_date")
    private LocalDate hireDate;

    @Column(name = "is_superintendent")
    private Boolean isSuperintendent = false; // Is this the superintendent pharmacist?

    // Status and Verification (in addition to User entity verification)
    @Enumerated(EnumType.STRING)
    @Column(name = "profile_status")
    private PharmacistStatus profileStatus = PharmacistStatus.PENDING_VERIFICATION;

    @Column(name = "profile_verification_date")
    private java.time.LocalDateTime profileVerificationDate;

    @Column(name = "suspension_reason", columnDefinition = "TEXT")
    private String suspensionReason;

    // Performance Metrics
    @Column(name = "total_prescriptions_dispensed")
    private Integer totalPrescriptionsDispensed = 0;

    @Column(name = "rating")
    private Double rating = 0.0; // Average rating from patients

    @Column(name = "total_reviews")
    private Integer totalReviews = 0;

    // Availability
    @Column(name = "is_available")
    private Boolean isAvailable = true; // Currently available to dispense prescriptions

    @Column(name = "availability_notes", columnDefinition = "TEXT")
    private String availabilityNotes; // e.g., "On leave until 2024-12-20"

    // Notes
    @Column(name = "admin_notes", columnDefinition = "TEXT")
    private String adminNotes; // Internal notes

    @Column(name = "bio", columnDefinition = "TEXT")
    private String bio; // Professional biography visible to patients

    @Column(name = "certificate_url")
    private String certificateUrl; // Link to license/certification document

    // Employment Type Enum
    public enum EmploymentType {
        FULL_TIME("Full-time employee"),
        PART_TIME("Part-time employee"),
        CONTRACT("Contract employee"),
        INTERN("Intern pharmacist"),
        LOCUM("Locum pharmacist");

        private final String description;

        EmploymentType(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    // Pharmacist Status Enum
    public enum PharmacistStatus {
        PENDING_VERIFICATION("Registration pending verification"),
        VERIFIED("Verified and active"),
        SUSPENDED("Temporarily suspended"),
        DEACTIVATED("Permanently deactivated"),
        LICENSE_EXPIRED("License has expired");

        private final String description;

        PharmacistStatus(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    // Override UserProfile abstract method
    @Override
    public boolean isProfileComplete() {
        return licenseNumber != null &&
               registrationNumber != null &&
               specialization != null &&
               phoneNumber != null &&
               dateOfBirth != null &&
               gender != null;
        // Note: pharmacy is optional - pharmacists can register without being assigned to a pharmacy
    }

    // Override UserProfile methods
    @Override
    public String getPhoneNumber() {
        return phoneNumber;
    }

    @Override
    public String getAddress() {
        return address;
    }

    // Business logic methods
    public boolean isVerified() {
        return profileStatus == PharmacistStatus.VERIFIED;
    }

    public boolean canDispensePrescriptions() {
        return Boolean.TRUE.equals(isAvailable) &&
               profileStatus == PharmacistStatus.VERIFIED &&
               getUser().getIsActive() &&
               !isLicenseExpired();
    }

    public boolean isLicenseExpired() {
        if (licenseExpiryDate == null) {
            return false;
        }
        return LocalDate.now().isAfter(licenseExpiryDate);
    }

    public void incrementPrescriptionCount() {
        this.totalPrescriptionsDispensed = (this.totalPrescriptionsDispensed != null ? this.totalPrescriptionsDispensed : 0) + 1;
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

    public int getAge() {
        if (dateOfBirth == null) {
            return 0;
        }
        return LocalDate.now().getYear() - dateOfBirth.getYear();
    }

    public boolean isLicenseValid() {
        return licenseNumber != null &&
               licenseNumber.length() >= 6 &&
               !isLicenseExpired();
    }

    @PreUpdate
    protected void onUpdate() {
        // Auto-update status if license expired
        if (isLicenseExpired() && profileStatus == PharmacistStatus.VERIFIED) {
            profileStatus = PharmacistStatus.LICENSE_EXPIRED;
        }
    }
}
