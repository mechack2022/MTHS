package com.mths.auth.dto;

import com.mths.auth.entity.User;
import com.mths.patient.dto.PatientProfileDTO;
import com.mths.hospital.dto.DoctorProfileDTO;

import java.util.List;

public class UserProfileDTO {

    private String userId;
    private String email;
    private String fullName;
    private User.AccountType accountType;
    private boolean hasPrimaryProfile;
    private boolean profileComplete;
    private List<String> missingFields;
    private PatientProfileDTO patientProfile;
    private DoctorProfileDTO doctorProfile;


    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public User.AccountType getAccountType() { return accountType; }
    public void setAccountType(User.AccountType accountType) { this.accountType = accountType; }

    public boolean isHasPrimaryProfile() { return hasPrimaryProfile; }
    public void setHasPrimaryProfile(boolean hasPrimaryProfile) { this.hasPrimaryProfile = hasPrimaryProfile; }

    public boolean isProfileComplete() { return profileComplete; }
    public void setProfileComplete(boolean profileComplete) { this.profileComplete = profileComplete; }

    public List<String> getMissingFields() { return missingFields; }
    public void setMissingFields(List<String> missingFields) { this.missingFields = missingFields; }

    public PatientProfileDTO getPatientProfile() { return patientProfile; }
    public void setPatientProfile(PatientProfileDTO patientProfile) { this.patientProfile = patientProfile; }

    public DoctorProfileDTO getDoctorProfile() { return doctorProfile; }
    public void setDoctorProfile(DoctorProfileDTO doctorProfile) { this.doctorProfile = doctorProfile; }
}
