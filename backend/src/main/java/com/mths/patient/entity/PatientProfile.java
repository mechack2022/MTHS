package com.mths.patient.entity;

import com.mths.auth.entity.UserProfile;
import com.mths.consultation.entity.Appointment;
import com.mths.shared.constants.Gender;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "patient_profiles")
@DiscriminatorValue("PATIENT")
@Data
public class PatientProfile extends UserProfile {

    @Column(name = "date_of_birth")
    private LocalDate dateOfBirth;

    @Enumerated(EnumType.STRING)
    private Gender gender;

    @Column(name = "nin", unique = true)
    private String NIN;

    @Column(name = "marital_status")
    private String maritalStatus;

    @Column(name = "blood_type")
    private String bloodType;

    @Column(name = "preferred_hospital")
    private String preferredHospital;

    @Column(name = "emergency_contact_name")
    private String emergencyContactName;

    @Column(name = "emergency_contact_phone")
    private String emergencyContactPhone;

    // Common fields that all profiles might have
    @Column(name = "phone_number", unique = true)
    private String phoneNumber;

    @Column(name = "address")
    private String address;

    @Column(name = "profile_image_url")
    private String profileImageUrl;

    // Appointment relationships
    @OneToMany(mappedBy = "patientProfile", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Appointment> appointments = new ArrayList<>();

    // TODO: Add when MedicalHistory and Notification entities are created
    // @OneToMany(mappedBy = "patientProfile", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    // private List<MedicalHistory> medicalHistories = new ArrayList<>();

    // @OneToMany(mappedBy = "patientProfile", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    // private List<Notification> notifications = new ArrayList<>();

    @Override
    public boolean isProfileComplete() {
        return dateOfBirth != null &&
                gender != null &&
                getPhoneNumber() != null &&
                getAddress() != null &&
                NIN != null;
    }

    public Integer getAge() {
        if (dateOfBirth != null) {
            return LocalDate.now().getYear() - dateOfBirth.getYear();
        }
        return null;
    }

    @Override
    public String getPhoneNumber() {
        return phoneNumber;
    }

    @Override
    public String getAddress() {
        return address;
    }

}
