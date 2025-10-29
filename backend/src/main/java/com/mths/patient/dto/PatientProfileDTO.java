package com.mths.patient.dto;

import com.mths.shared.constants.Gender;

import java.time.LocalDate;

public class PatientProfileDTO {

    private Long id;
    private LocalDate dateOfBirth;
    private Gender gender;
    private String NIN;
    private String maritalStatus;
//    private String bloodType;
    private String preferredHospital;
    private String emergencyContactName;
    private String emergencyContactPhone;
    private String phoneNumber;
    private String address;
    private String profileImageUrl;
    private boolean profileComplete;
    private Integer age;

    // Getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public LocalDate getDateOfBirth() { return dateOfBirth; }
    public void setDateOfBirth(LocalDate dateOfBirth) { this.dateOfBirth = dateOfBirth; }

    public Gender getGender() { return gender; }
    public void setGender(Gender gender) { this.gender = gender; }

    public String getNIN() { return NIN; }
    public void setNIN(String NIN) { this.NIN = NIN; }

    public String getMaritalStatus() { return maritalStatus; }
    public void setMaritalStatus(String maritalStatus) { this.maritalStatus = maritalStatus; }

//    public String getBloodType() { return bloodType; }
//    public void setBloodType(String bloodType) { this.bloodType = bloodType; }

    public String getPreferredHospital() { return preferredHospital; }
    public void setPreferredHospital(String preferredHospital) { this.preferredHospital = preferredHospital; }

    public String getEmergencyContactName() { return emergencyContactName; }
    public void setEmergencyContactName(String emergencyContactName) { this.emergencyContactName = emergencyContactName; }

    public String getEmergencyContactPhone() { return emergencyContactPhone; }
    public void setEmergencyContactPhone(String emergencyContactPhone) { this.emergencyContactPhone = emergencyContactPhone; }

    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getProfileImageUrl() { return profileImageUrl; }
    public void setProfileImageUrl(String profileImageUrl) { this.profileImageUrl = profileImageUrl; }

    public boolean isProfileComplete() { return profileComplete; }
    public void setProfileComplete(boolean profileComplete) { this.profileComplete = profileComplete; }

    public Integer getAge() { return age; }
    public void setAge(Integer age) { this.age = age; }
}
