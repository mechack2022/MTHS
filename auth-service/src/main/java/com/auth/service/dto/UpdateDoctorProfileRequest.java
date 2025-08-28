package com.auth.service.dto;
import com.auth.service.constants.Gender;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.URL;
@Setter
@Getter
public class UpdateDoctorProfileRequest {
    @Size(min = 6, max = 50, message = "Medical license number must be between 6 and 50 characters")
    private String medicalLicenseNumber;

    @Size(max = 100, message = "Specialization must be less than 100 characters")
    private String specialization;

    @Min(value = 0, message = "Years of experience cannot be negative")
    @Max(value = 60, message = "Years of experience cannot exceed 60")
    private Integer yearsOfExperience;

    @Size(max = 100, message = "Hospital affiliation must be less than 100 characters")
    private String hospitalAffiliation;

//    @DecimalMin(value = "0.0", message = "Consultation fee cannot be negative")
//    private Double consultationFee;

    private Boolean availableForConsultation;

    @Size(max = 100, message = "Office hours must be less than 100 characters")
    private String officeHours;

    @Size(max = 100, message = "Medical school name must be less than 100 characters")
    private String medicalSchool;

    @Size(max = 500, message = "Board certifications must be less than 500 characters")
    private String boardCertifications;

    @Pattern(regexp = "^\\+?[1-9]\\d{1,14}$", message = "Invalid phone number format")
    private String phoneNumber;

    @Size(max = 500, message = "Address must be less than 500 characters")
    private String address;

    @URL(message = "Profile image URL must be valid")
    private String profileImageUrl;
}
