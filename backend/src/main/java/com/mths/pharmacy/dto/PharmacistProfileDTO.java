package com.mths.pharmacy.dto;

import com.mths.pharmacy.entity.Pharmacist;
import com.mths.shared.constants.Gender;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Schema(description = "Pharmacist profile details")
public class PharmacistProfileDTO {

    @Schema(description = "Profile ID", example = "1")
    private Long id;

    @Schema(description = "User ID", example = "uuid-123-456")
    private String userId;

    @Schema(description = "Email", example = "pharmacist@example.com")
    private String email;

    @Schema(description = "Full name", example = "John Doe")
    private String fullName;

    @Schema(description = "Pharmacy ID", example = "1")
    private Long pharmacyId;

    @Schema(description = "Pharmacy name", example = "HealthPlus Pharmacy")
    private String pharmacyName;

    @Schema(description = "License number", example = "PCN-123456")
    private String licenseNumber;

    @Schema(description = "Registration number", example = "REG-789012")
    private String registrationNumber;

    @Schema(description = "License expiry date", example = "2026-12-31")
    private LocalDate licenseExpiryDate;

    @Schema(description = "Issuing authority", example = "Pharmacists Council of Nigeria")
    private String issuingAuthority;

    @Schema(description = "Gender", example = "MALE")
    private Gender gender;

    @Schema(description = "Date of birth", example = "1990-01-15")
    private LocalDate dateOfBirth;

    @Schema(description = "Age", example = "34")
    private Integer age;

    @Schema(description = "Phone number", example = "+234-801-234-5678")
    private String phoneNumber;

    @Schema(description = "Alternative phone", example = "+234-802-345-6789")
    private String alternativePhone;

    @Schema(description = "Address", example = "123 Pharmacy Street, Lagos")
    private String address;

    @Schema(description = "Specialization", example = "Clinical Pharmacy")
    private String specialization;

    @Schema(description = "Years of experience", example = "5")
    private Integer yearsOfExperience;

    @Schema(description = "Qualifications", example = "B.Pharm, M.Sc. Clinical Pharmacy")
    private String qualifications;

    @Schema(description = "Pharmacy school", example = "University of Lagos")
    private String pharmacySchool;

    @Schema(description = "Graduation year", example = "2018")
    private Integer graduationYear;

    @Schema(description = "Employment type", example = "FULL_TIME")
    private Pharmacist.EmploymentType employmentType;

    @Schema(description = "Position", example = "Staff Pharmacist")
    private String position;

    @Schema(description = "Hire date", example = "2020-01-01")
    private LocalDate hireDate;

    @Schema(description = "Is superintendent pharmacist", example = "false")
    private Boolean isSuperintendent;

    @Schema(description = "Profile status", example = "VERIFIED")
    private Pharmacist.PharmacistStatus profileStatus;

    @Schema(description = "Verification date", example = "2024-12-13T10:30:00")
    private LocalDateTime profileVerificationDate;

    @Schema(description = "Total prescriptions dispensed", example = "150")
    private Integer totalPrescriptionsDispensed;

    @Schema(description = "Rating", example = "4.5")
    private Double rating;

    @Schema(description = "Total reviews", example = "30")
    private Integer totalReviews;

    @Schema(description = "Is available", example = "true")
    private Boolean isAvailable;

    @Schema(description = "Availability notes", example = "Available during regular business hours")
    private String availabilityNotes;

    @Schema(description = "Bio", example = "Experienced pharmacist specializing in clinical pharmacy")
    private String bio;

    @Schema(description = "Certificate URL", example = "https://storage.example.com/certificates/pharmacist123.pdf")
    private String certificateUrl;

    @Schema(description = "Profile image URL", example = "https://storage.example.com/profiles/pharmacist123.jpg")
    private String profileImageUrl;

    @Schema(description = "Is profile complete", example = "true")
    private Boolean isProfileComplete;

    @Schema(description = "Is verified", example = "true")
    private Boolean isVerified;

    @Schema(description = "Can dispense prescriptions", example = "true")
    private Boolean canDispensePrescriptions;

    @Schema(description = "Is license valid", example = "true")
    private Boolean isLicenseValid;

    @Schema(description = "Is license expired", example = "false")
    private Boolean isLicenseExpired;

    @Schema(description = "Created at", example = "2024-01-01T10:00:00")
    private LocalDateTime createdAt;

    @Schema(description = "Updated at", example = "2024-12-13T15:30:00")
    private LocalDateTime updatedAt;
}
