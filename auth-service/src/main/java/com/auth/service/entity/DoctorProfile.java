package com.auth.service.entity;

import com.auth.service.constants.Gender;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

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

    @Column(name = "experience", columnDefinition = "TEXT")
    private String experience; // Short note about doctor's experience

    @Column(name = "bio", columnDefinition = "TEXT")
    private String bio;

    @Column(name = "practice_address")
    private String practiceAddress;

    @Column(name = "date_of_birth")
    private LocalDate dateOfBirth;

    @Enumerated(EnumType.STRING)
    @Column(name = "gender")
    private Gender gender;

    @Column(name = "certificate_url")
    private String certificateUrl;

    @Column(name = "board_certifications")
    private String boardCertifications;

    @Column(name = "phone_number", unique = true)
    private String phoneNumber;

    @Column(name = "address")
    private String address;

    @Column(name = "profile_image_url")
    private String profileImageUrl;

    // Appointment relationships
    @OneToMany(mappedBy = "doctorProfile", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Appointment> appointments = new ArrayList<>();

    // TODO: Add when Prescription and LabOrder entities are created
    // @OneToMany(mappedBy = "doctorProfile", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    // private List<Prescription> prescriptions = new ArrayList<>();

    // @OneToMany(mappedBy = "doctorProfile", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    // private List<LabOrder> labOrders = new ArrayList<>();


    @Override
    public boolean isProfileComplete() {
        return medicalLicenseNumber != null &&
                specialization != null &&
                getPhoneNumber() != null &&
                practiceAddress != null &&
                dateOfBirth != null &&
                gender != null;
    }

    // Business logic methods
    public boolean isLicenseValid() {
        // Add license validation logic
        return medicalLicenseNumber != null && medicalLicenseNumber.length() >= 6;
    }

    public boolean canTakeNewPatients() {
        return getUser().getIsActive() && isProfileComplete();
    }

    @Override
    public String getPhoneNumber() {
        return phoneNumber;
    }

    @Override
    public String getAddress() {
        return practiceAddress != null ? practiceAddress : address;
    }


}

