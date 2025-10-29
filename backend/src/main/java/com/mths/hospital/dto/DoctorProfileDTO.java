package com.mths.hospital.dto;

import com.mths.shared.constants.Gender;
import java.time.LocalDate;

public class DoctorProfileDTO {
    private Long id;
    private String medicalLicenseNumber;
    private String specialization;
    private Integer yearsOfExperience;
    private String experience;
    private String bio;
    private String practiceAddress;
    private LocalDate dateOfBirth;
    private Gender gender;
    private String certificateUrl;
    private String boardCertifications;
    private String phoneNumber;
    private String address;
    private String profileImageUrl;
    private boolean profileComplete;
    private boolean licenseValid;
    private boolean canTakeNewPatients;

    // Getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

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

    public boolean isProfileComplete() { return profileComplete; }
    public void setProfileComplete(boolean profileComplete) { this.profileComplete = profileComplete; }

    public boolean isLicenseValid() { return licenseValid; }
    public void setLicenseValid(boolean licenseValid) { this.licenseValid = licenseValid; }

    public boolean isCanTakeNewPatients() { return canTakeNewPatients; }
    public void setCanTakeNewPatients(boolean canTakeNewPatients) { this.canTakeNewPatients = canTakeNewPatients; }
}
