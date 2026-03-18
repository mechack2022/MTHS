package com.mths.pharmacy.dto;

import com.mths.pharmacy.entity.Pharmacy;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * Response DTO for pharmacy
 */
@Data
@Schema(description = "Pharmacy details")
public class PharmacyResponse {

    @Schema(description = "Pharmacy ID", example = "1")
    private Long id;

    @Schema(description = "Pharmacy name", example = "HealthPlus Pharmacy")
    private String pharmacyName;

    @Schema(description = "License number", example = "PH-12345-NG")
    private String licenseNumber;

    @Schema(description = "Registration number", example = "PCN-67890")
    private String registrationNumber;

    @Schema(description = "Email address", example = "contact@healthplus.com")
    private String email;

    @Schema(description = "Primary phone number", example = "+2348012345678")
    private String phoneNumber;

    @Schema(description = "Alternative phone number", example = "+2347098765432")
    private String alternativePhone;

    @Schema(description = "Street address", example = "123 Main Street")
    private String address;

    @Schema(description = "City", example = "Lagos")
    private String city;

    @Schema(description = "State", example = "Lagos State")
    private String state;

    @Schema(description = "Postal code", example = "100001")
    private String postalCode;

    @Schema(description = "Country", example = "Nigeria")
    private String country;

    @Schema(description = "Full formatted address")
    private String fullAddress;

    @Schema(description = "Latitude", example = "6.5244")
    private Double latitude;

    @Schema(description = "Longitude", example = "3.3792")
    private Double longitude;

    @Schema(description = "Opening time", example = "08:00:00")
    private LocalTime openingTime;

    @Schema(description = "Closing time", example = "20:00:00")
    private LocalTime closingTime;

    @Schema(description = "Is open 24 hours?", example = "false")
    private Boolean is24Hours;

    @Schema(description = "Operating days", example = "Mon-Sat")
    private String operatingDays;

    @Schema(description = "Owner name", example = "Dr. John Doe")
    private String ownerName;

    @Schema(description = "Superintendent pharmacist", example = "Pharm. Jane Smith")
    private String superintendentPharmacist;

    @Schema(description = "Services offered")
    private String servicesOffered;

    @Schema(description = "Accepts insurance?", example = "true")
    private Boolean acceptsInsurance;

    @Schema(description = "Insurance providers")
    private String insuranceProviders;

    @Schema(description = "Pharmacy status", example = "VERIFIED")
    private Pharmacy.PharmacyStatus status;

    @Schema(description = "Verification date")
    private LocalDateTime verificationDate;

    @Schema(description = "Verified by (admin)")
    private String verifiedBy;

    @Schema(description = "Is active?", example = "true")
    private Boolean isActive;

    @Schema(description = "Deactivation reason")
    private String deactivationReason;

    @Schema(description = "Average rating (0-5)", example = "4.5")
    private Double rating;

    @Schema(description = "Total number of reviews", example = "120")
    private Integer totalReviews;

    @Schema(description = "Total prescriptions filled", example = "500")
    private Integer totalPrescriptionsFilled;

    @Schema(description = "Public notes")
    private String publicNotes;

    @Schema(description = "Is verified?", example = "true")
    private Boolean isVerified;

    @Schema(description = "Can accept prescriptions?", example = "true")
    private Boolean canAcceptPrescriptions;

    @Schema(description = "Is currently open?", example = "true")
    private Boolean isCurrentlyOpen;

    @Schema(description = "Record creation timestamp")
    private LocalDateTime createdAt;

    @Schema(description = "Record last update timestamp")
    private LocalDateTime updatedAt;
}
