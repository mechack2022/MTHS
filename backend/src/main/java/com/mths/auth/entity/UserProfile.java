package com.mths.auth.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

@Entity
@Table(name = "user_profiles")
@Inheritance(strategy = InheritanceType.JOINED)
@DiscriminatorColumn(name = "profile_type", discriminatorType = DiscriminatorType.STRING)
public abstract class UserProfile extends com.mths.shared.entity.BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @JsonIgnore
    private User user;

    @Column(name = "profile_type", insertable = false, updatable = false)
    @Enumerated(EnumType.STRING)
    private ProfileType profileType;

    @Column(name = "profile_image_url")
    private String profileImageUrl;

    // Abstract method that each profile must implement
    public abstract boolean isProfileComplete();

    // Getters and setters
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public ProfileType getProfileType() { return profileType; }
    public void setProfileType(ProfileType profileType) { this.profileType = profileType; }

    public String getProfileImageUrl() { return profileImageUrl; }
    public void setProfileImageUrl(String profileImageUrl) { this.profileImageUrl = profileImageUrl; }

    public String getFullName() {
        return user != null ? user.getFirstName() + " " + user.getLastName(): null;
    }

    public String getEmail() {
        return user != null ? user.getEmail() : null;
    }

    // Override in child classes for specific implementations
    public String getPhoneNumber() {
        return null; // To be overridden by child classes
    }

    public String getAddress() {
        return null; // To be overridden by child classes
    }

    public enum ProfileType {
        PATIENT, DOCTOR, ADMIN, PHARMACY_OWNER, LAB_TECHNICIAN, PHARMACIST
    }
}