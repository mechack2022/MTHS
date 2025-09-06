package com.auth.service.dto;

import com.auth.service.constants.Gender;
import jakarta.validation.constraints.*;
import org.hibernate.validator.constraints.URL;

import java.time.LocalDate;

public class CreateDoctorProfileRequest {

    @NotBlank(message = "Medical license number is required")
    @Size(min = 6, max = 50, message = "Medical license number must be between 6 and 50 characters")
    private String medicalLicenseNumber;

    @NotBlank(message = "Specialization is required")
    @Size(max = 100, message = "Specialization must be less than 100 characters")
    private String specialization;

    @Min(value = 0, message = "Years of experience cannot be negative")
    @Max(value = 60, message = "Years of experience cannot exceed 60")
    private Integer yearsOfExperience;

    @Size(max = 1000, message = "Experience must be less than 1000 characters")
    private String experience;

    @Size(max = 2000, message = "Bio must be less than 2000 characters")
    private String bio;

    @NotBlank(message = "Practice address is required")
    @Size(max = 500, message = "Practice address must be less than 500 characters")
    private String practiceAddress;

    @NotNull(message = "Date of birth is required")
    @Past(message = "Date of birth must be in the past")
    private LocalDate dateOfBirth;

    @NotNull(message = "Gender is required")
    private Gender gender;

    @URL(message = "Certificate URL must be valid")
    private String certificateUrl;

    @Size(max = 500, message = "Board certifications must be less than 500 characters")
    private String boardCertifications;

    @NotBlank(message = "Phone number is required")
    @Pattern(regexp = "^\\+?[1-9]\\d{1,14}$", message = "Invalid phone number format")
    private String phoneNumber;

    @Size(max = 500, message = "Address must be less than 500 characters")
    private String address;

    @URL(message = "Profile image URL must be valid")
    private String profileImageUrl;

    // Getters and setters
    public String getMedicalLicenseNumber() { return medicalLicenseNumber; }
    public void setMedicalLicenseNumber(String medicalLicenseNumber) { this.medicalLicenseNumber = medicalLicenseNumber; }

    public String getSpecialization() { return specialization; }
    public void setSpecialization(String specialization) { this.specialization = specialization; }

    public Integer getYearsOfExperience() { return yearsOfExperience; }
    public void setYearsOfExperience(Integer yearsOfExperience) { this.yearsOfExperience = yearsOfExperience; }

    public String getExperience() { return experience; }
    public void setExperience(String experience) { this.experience = experience; }

    public String getBio() { return bio; }
    public void setBio(String bio) { this.bio = bio; }

    public String getPracticeAddress() { return practiceAddress; }
    public void setPracticeAddress(String practiceAddress) { this.practiceAddress = practiceAddress; }

    public LocalDate getDateOfBirth() { return dateOfBirth; }
    public void setDateOfBirth(LocalDate dateOfBirth) { this.dateOfBirth = dateOfBirth; }

    public Gender getGender() { return gender; }
    public void setGender(Gender gender) { this.gender = gender; }

    public String getCertificateUrl() { return certificateUrl; }
    public void setCertificateUrl(String certificateUrl) { this.certificateUrl = certificateUrl; }

    public String getBoardCertifications() { return boardCertifications; }
    public void setBoardCertifications(String boardCertifications) { this.boardCertifications = boardCertifications; }

    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getProfileImageUrl() { return profileImageUrl; }
    public void setProfileImageUrl(String profileImageUrl) { this.profileImageUrl = profileImageUrl; }
}
