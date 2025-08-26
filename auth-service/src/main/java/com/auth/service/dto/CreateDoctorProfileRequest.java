package com.auth.service.dto;

import com.auth.service.constants.Gender;
import jakarta.validation.constraints.*;
import org.hibernate.validator.constraints.URL;

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

    @NotBlank(message = "Hospital affiliation is required")
    @Size(max = 100, message = "Hospital affiliation must be less than 100 characters")
    private String hospitalAffiliation;

    @DecimalMin(value = "0.0", message = "Consultation fee cannot be negative")
    private Double consultationFee;

    private Boolean availableForConsultation = true;

    @Size(max = 100, message = "Office hours must be less than 100 characters")
    private String officeHours;

    @Size(max = 100, message = "Medical school name must be less than 100 characters")
    private String medicalSchool;

    @Size(max = 500, message = "Board certifications must be less than 500 characters")
    private String boardCertifications;

    @NotBlank(message = "Phone number is required")
    @Pattern(regexp = "^\\+?[1-9]\\d{1,14}$", message = "Invalid phone number format")
    private String phoneNumber;

    @NotBlank(message = "Address is required")
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

    public String getHospitalAffiliation() { return hospitalAffiliation; }
    public void setHospitalAffiliation(String hospitalAffiliation) { this.hospitalAffiliation = hospitalAffiliation; }

    public Double getConsultationFee() { return consultationFee; }
    public void setConsultationFee(Double consultationFee) { this.consultationFee = consultationFee; }

    public Boolean getAvailableForConsultation() { return availableForConsultation; }
    public void setAvailableForConsultation(Boolean availableForConsultation) { this.availableForConsultation = availableForConsultation; }

    public String getOfficeHours() { return officeHours; }
    public void setOfficeHours(String officeHours) { this.officeHours = officeHours; }

    public String getMedicalSchool() { return medicalSchool; }
    public void setMedicalSchool(String medicalSchool) { this.medicalSchool = medicalSchool; }

    public String getBoardCertifications() { return boardCertifications; }
    public void setBoardCertifications(String boardCertifications) { this.boardCertifications = boardCertifications; }

    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getProfileImageUrl() { return profileImageUrl; }
    public void setProfileImageUrl(String profileImageUrl) { this.profileImageUrl = profileImageUrl; }
}
