package com.mths.patient.dto;
import com.mths.shared.constants.Gender;
import com.mths.auth.entity.Role;
import com.mths.auth.entity.User;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.*;


import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;
public class PatientDto {
    private Long id;
    private Long userId;
    private String userUuid;

    // User fields (flattened from User entity)
    @Email(message = "Invalid email format")
    private String email;

    @NotBlank(message = "First name is required")
    private String firstName;

    @NotBlank(message = "Last name is required")
    private String lastName;

    private Boolean mailVerified;
    private User.AccountType accountType;
    private Boolean isActive;
    private Boolean accountVerified;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate dateOfBirth;

    private Gender gender;

    @Pattern(regexp = "^\\+?[1-9]\\d{1,14}$", message = "Invalid phone number format")
    private String phoneNumber;

    private String NIN;
    private String address;
    private String maritalStatus;
    private String profileImageUrl;
    private String preferredHospital;
    private String preferredLaboratory;
    private String preferredPharmacy;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;
    // Include roles when needed
    private Set<Role> roles;

}
