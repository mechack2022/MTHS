package com.mths.hospital.dto;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateLabTechnicianProfileRequest {

    @Size(min = 6, max = 20, message = "Certification number must be between 6 and 20 characters")
    private String certificationNumber;

    @Size(max = 100, message = "Specialization cannot exceed 100 characters")
    private String specialization;

    private Integer yearsOfExperience;

    @Size(max = 200, message = "Laboratory affiliation cannot exceed 200 characters")
    private String laboratoryAffiliation;

    @Size(max = 100, message = "Certifying body cannot exceed 100 characters")
    private String certifyingBody;

    private LocalDate certificationExpiryDate;

    private Boolean availableForDuty;

    @Size(max = 50, message = "Work shift cannot exceed 50 characters")
    private String workShift;

    @Size(max = 500, message = "Equipment specialties cannot exceed 500 characters")
    private String equipmentSpecialties;

    @Pattern(regexp = "^[+]?[0-9]{10,15}$", message = "Invalid phone number format")
    private String phoneNumber;

    @Size(max = 500, message = "Address cannot exceed 500 characters")
    private String address;

    private String profileImageUrl;
}