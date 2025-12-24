package com.mths.pharmacy.dto;

import com.mths.pharmacy.entity.Pharmacist;
import com.mths.shared.constants.Gender;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.LocalDate;

@Data
@Schema(description = "Request to update a pharmacist profile")
public class UpdatePharmacistProfileRequest {

    @Schema(description = "Pharmacy ID where pharmacist works", example = "1")
    private Long pharmacyId;

    @Schema(description = "License expiry date", example = "2026-12-31")
    @Future(message = "License expiry date must be in the future")
    private LocalDate licenseExpiryDate;

    @Schema(description = "Issuing authority", example = "Pharmacists Council of Nigeria")
    private String issuingAuthority;

    @Schema(description = "Gender", example = "MALE")
    private Gender gender;

    @Schema(description = "Date of birth", example = "1990-01-15")
    @Past(message = "Date of birth must be in the past")
    private LocalDate dateOfBirth;

    @Schema(description = "Phone number", example = "+234-801-234-5678")
    private String phoneNumber;

    @Schema(description = "Alternative phone number", example = "+234-802-345-6789")
    private String alternativePhone;

    @Schema(description = "Address", example = "123 Pharmacy Street, Lagos")
    private String address;

    @Schema(description = "Specialization", example = "Clinical Pharmacy")
    private String specialization;

    @Schema(description = "Years of experience", example = "5")
    @Min(value = 0, message = "Years of experience cannot be negative")
    private Integer yearsOfExperience;

    @Schema(description = "Qualifications", example = "B.Pharm, M.Sc. Clinical Pharmacy")
    private String qualifications;

    @Schema(description = "Pharmacy school", example = "University of Lagos")
    private String pharmacySchool;

    @Schema(description = "Graduation year", example = "2018")
    @Min(value = 1950, message = "Graduation year must be after 1950")
    @Max(value = 2030, message = "Graduation year must be before 2030")
    private Integer graduationYear;

    @Schema(description = "Employment type", example = "FULL_TIME")
    private Pharmacist.EmploymentType employmentType;

    @Schema(description = "Position/title", example = "Staff Pharmacist")
    private String position;

    @Schema(description = "Hire date", example = "2020-01-01")
    private LocalDate hireDate;

    @Schema(description = "Is superintendent pharmacist?", example = "false")
    private Boolean isSuperintendent;

    @Schema(description = "Is available for dispensing", example = "true")
    private Boolean isAvailable;

    @Schema(description = "Availability notes", example = "Available during regular business hours")
    private String availabilityNotes;

    @Schema(description = "Professional bio", example = "Experienced pharmacist with focus on patient care")
    private String bio;

    @Schema(description = "Certificate URL", example = "https://storage.example.com/certificates/pharmacist123.pdf")
    private String certificateUrl;
}
