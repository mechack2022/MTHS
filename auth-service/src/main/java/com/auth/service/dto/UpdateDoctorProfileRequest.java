package com.auth.service.dto;
import com.auth.service.constants.Gender;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.URL;

import java.time.LocalDate;
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

    @Size(max = 1000, message = "Experience must be less than 1000 characters")
    private String experience;

    @Size(max = 2000, message = "Bio must be less than 2000 characters")
    private String bio;

    @Size(max = 500, message = "Practice address must be less than 500 characters")
    private String practiceAddress;

    @Past(message = "Date of birth must be in the past")
    private LocalDate dateOfBirth;

    private Gender gender;

    @URL(message = "Certificate URL must be valid")
    private String certificateUrl;

    @Size(max = 500, message = "Board certifications must be less than 500 characters")
    private String boardCertifications;

    @Pattern(regexp = "^\\+?[1-9]\\d{1,14}$", message = "Invalid phone number format")
    private String phoneNumber;

    @Size(max = 500, message = "Address must be less than 500 characters")
    private String address;

    @URL(message = "Profile image URL must be valid")
    private String profileImageUrl;
}
