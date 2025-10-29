package com.mths.patient.dto;

import java.time.LocalDate;

import com.mths.shared.constants.Gender;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.URL;
@Setter
@Getter
public class UpdatePatientProfileRequest {

    private LocalDate dateOfBirth;
    private Gender gender;

    @Size(min = 11, max = 11, message = "NIN must be exactly 11 digits")
    private String NIN;

    private String maritalStatus;
    private String bloodType;
    private String preferredHospital;
    private String emergencyContactName;

    @Pattern(regexp = "^\\+?[1-9]\\d{1,14}$", message = "Invalid emergency contact phone number")
    private String emergencyContactPhone;

    @Pattern(regexp = "^\\+?[1-9]\\d{1,14}$", message = "Invalid phone number format")
    private String phoneNumber;

    @Size(max = 500, message = "Address must be less than 500 characters")
    private String address;

    @URL(message = "Profile image URL must be valid")
    private String profileImageUrl;
}
