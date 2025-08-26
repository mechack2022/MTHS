package com.auth.service.entity;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "doctor_profiles")
@DiscriminatorValue("DOCTOR")
@Setter
@Getter
public class DoctorProfile extends UserProfile {

    @Column(name = "medical_license_number", unique = true)
    private String medicalLicenseNumber;

    @Column(name = "specialization")
    private String specialization;

    @Column(name = "years_of_experience")
    private Integer yearsOfExperience;

    @Column(name = "hospital_affiliation")
    private String hospitalAffiliation;

    @Column(name = "consultation_fee")
    private Double consultationFee;

    @Column(name = "is_available_for_consultation")
    private Boolean availableForConsultation = true;

    @Column(name = "office_hours")
    private String officeHours;

    @Column(name = "medical_school")
    private String medicalSchool;

    @Column(name = "board_certifications")
    private String boardCertifications;

    @Column(name = "phone_number", unique = true)
    private String phoneNumber;

    @Column(name = "address")
    private String address;

    @Column(name = "profile_image_url")
    private String profileImageUrl;


    @Override
    public boolean isProfileComplete() {
        return medicalLicenseNumber != null &&
                specialization != null &&
                getPhoneNumber() != null &&
                hospitalAffiliation != null;
    }

    // Business logic methods
    public boolean isLicenseValid() {
        // Add license validation logic
        return medicalLicenseNumber != null && medicalLicenseNumber.length() >= 6;
    }

    public boolean canTakeNewPatients() {
        return availableForConsultation && getUser().getIsActive();
    }

    @Override
    public String getPhoneNumber() {
        return phoneNumber;
    }

    @Override
    public String getAddress() {
        return address;
    }

    // Getters and setters...
}

