package com.mths.pharmacy.dto;

import com.mths.pharmacy.entity.Pharmacy;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.LocalTime;

/**
 * Request DTO for creating a new pharmacy
 */
@Data
@Schema(description = "Request to create a new pharmacy")
public class CreatePharmacyRequest {

    @Schema(description = "Pharmacy name", example = "HealthPlus Pharmacy", required = true)
    @NotBlank(message = "Pharmacy name is required")
    @Size(min = 3, max = 200, message = "Pharmacy name must be between 3 and 200 characters")
    private String pharmacyName;

    @Schema(description = "Pharmacy license number", example = "PH-12345-NG", required = true)
    @NotBlank(message = "License number is required")
    private String licenseNumber;

    @Schema(description = "Pharmacy registration number", example = "PCN-67890")
    private String registrationNumber;

    @Schema(description = "Pharmacy email address", example = "contact@healthplus.com", required = true)
    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;

    @Schema(description = "Primary phone number", example = "+2348012345678", required = true)
    @NotBlank(message = "Phone number is required")
    private String phoneNumber;

    @Schema(description = "Alternative phone number", example = "+2347098765432")
    private String alternativePhone;

    @Schema(description = "Street address", example = "123 Main Street", required = true)
    @NotBlank(message = "Address is required")
    private String address;

    @Schema(description = "City", example = "Lagos", required = true)
    @NotBlank(message = "City is required")
    private String city;

    @Schema(description = "State", example = "Lagos State", required = true)
    @NotBlank(message = "State is required")
    private String state;

    @Schema(description = "Postal code", example = "100001")
    private String postalCode;

    @Schema(description = "Country", example = "Nigeria")
    private String country = "Nigeria";

    @Schema(description = "Latitude for location", example = "6.5244")
    private Double latitude;

    @Schema(description = "Longitude for location", example = "3.3792")
    private Double longitude;

    @Schema(description = "Opening time", example = "08:00:00")
    private LocalTime openingTime;

    @Schema(description = "Closing time", example = "20:00:00")
    private LocalTime closingTime;

    @Schema(description = "Is the pharmacy open 24 hours?", example = "false")
    private Boolean is24Hours = false;

    @Schema(description = "Operating days", example = "Mon-Sat")
    private String operatingDays;

    @Schema(description = "Owner name", example = "Dr. John Doe")
    private String ownerName;

    @Schema(description = "Superintendent pharmacist name", example = "Pharm. Jane Smith")
    private String superintendentPharmacist;

    @Schema(description = "Services offered", example = "Prescription filling, OTC medications, Health consultations")
    private String servicesOffered;

    @Schema(description = "Does pharmacy accept insurance?", example = "true")
    private Boolean acceptsInsurance = false;

    @Schema(description = "Insurance providers accepted", example = "NHIS, Reliance HMO, AXA Mansard")
    private String insuranceProviders;

    @Schema(description = "Public notes visible to patients", example = "Located near General Hospital")
    private String publicNotes;
}
