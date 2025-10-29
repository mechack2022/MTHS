package com.mths.hospital.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LabTechnicianProfileDTO {
    private Long id;
    private String certificationNumber;
    private String specialization;
    private Integer yearsOfExperience;
    private String laboratoryAffiliation;
    private String certifyingBody;
    private LocalDate certificationExpiryDate;
    private Boolean availableForDuty;
    private String workShift;
    private String equipmentSpecialties;
    private String phoneNumber;
    private String address;
    private String profileImageUrl;
    private Boolean profileComplete;
    private Boolean certificationValid;
    
    // User information
    private String email;
    private String fullName;
    private String firstName;
    private String lastName;
    private Boolean isActive;
}