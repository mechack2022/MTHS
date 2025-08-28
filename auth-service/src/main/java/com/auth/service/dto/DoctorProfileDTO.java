package com.auth.service.dto;

public class DoctorProfileDTO {
    private Long id;
    private String medicalLicenseNumber;
    private String specialization;
    private Integer yearsOfExperience;
//    private String hospitalAffiliation;
//    private Double consultationFee;
//    private Boolean availableForConsultation;
//    private String officeHours;
//    private String medicalSchool;
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

//    public String getHospitalAffiliation() { return hospitalAffiliation; }
//    public void setHospitalAffiliation(String hospitalAffiliation) { this.hospitalAffiliation = hospitalAffiliation; }
//
//    public Double getConsultationFee() { return consultationFee; }
//    public void setConsultationFee(Double consultationFee) { this.consultationFee = consultationFee; }
//
//    public Boolean getAvailableForConsultation() { return availableForConsultation; }
//    public void setAvailableForConsultation(Boolean availableForConsultation) { this.availableForConsultation = availableForConsultation; }
//
//    public String getOfficeHours() { return officeHours; }
//    public void setOfficeHours(String officeHours) { this.officeHours = officeHours; }
//
//    public String getMedicalSchool() { return medicalSchool; }
//    public void setMedicalSchool(String medicalSchool) { this.medicalSchool = medicalSchool; }

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
