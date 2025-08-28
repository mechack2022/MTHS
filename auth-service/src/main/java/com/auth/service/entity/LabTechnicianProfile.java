package com.auth.service.entity;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "lab_technician_profiles")
@DiscriminatorValue("LAB_TECHNICIAN")
@Setter
@Getter
public class LabTechnicianProfile extends UserProfile {

    @Column(name = "certification_number", unique = true)
    private String certificationNumber;

    @Column(name = "specialization")
    private String specialization;

    @Column(name = "years_of_experience")
    private Integer yearsOfExperience;

    @Column(name = "laboratory_affiliation")
    private String laboratoryAffiliation;

    @Column(name = "certifying_body")
    private String certifyingBody;

    @Column(name = "certification_expiry_date")
    private java.time.LocalDate certificationExpiryDate;

    @Column(name = "is_available_for_duty")
    private Boolean availableForDuty = true;

    @Column(name = "work_shift")
    private String workShift;

    @Column(name = "equipment_specialties")
    private String equipmentSpecialties;

    @Column(name = "phone_number", unique = true)
    private String phoneNumber;

    @Column(name = "address")
    private String address;

    @Column(name = "profile_image_url")
    private String profileImageUrl;

    @Override
    public boolean isProfileComplete() {
        return certificationNumber != null &&
                specialization != null &&
                getPhoneNumber() != null &&
                laboratoryAffiliation != null &&
                certifyingBody != null;
    }

    public boolean isCertificationValid() {
        return certificationNumber != null && 
               certificationNumber.length() >= 6 &&
               (certificationExpiryDate == null || 
                certificationExpiryDate.isAfter(java.time.LocalDate.now()));
    }

    public boolean canTakeNewAssignments() {
        return availableForDuty && getUser().getIsActive() && isCertificationValid();
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