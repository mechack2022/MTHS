package com.auth.service.entity;

//import com.auth.service.entity.doctor.Doctor;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User extends BaseEntity {

    @Column(name="uuid", nullable = false, unique = true)
    private String uuid;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(name = "first_name")
    private String firstName;

    @Column(name = "last_name")
    private String lastName;

    @Column(name = "mail_verified", nullable = false)
    private Boolean mailVerified = false;

    @Column(name = "account_type")
    @Enumerated(EnumType.STRING)
    private AccountType accountType;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @Column(name = "acccount_verified", nullable = false)
    private Boolean accountVerified = false;

    // Many-to-Many relationship with Role
    @ManyToMany(fetch = FetchType.EAGER, cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinTable(
            name = "user_roles",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    private Set<Role> roles = new HashSet<>();

//    @ManyToMany(fetch = FetchType.EAGER, cascade = {CascadeType.PERSIST, CascadeType.MERGE})
//    @JoinTable(
//            name = "user_doctor",
//            joinColumns = @JoinColumn(name = "user_id"),
//            inverseJoinColumns = @JoinColumn(name = "doctor_id")
//    )
//    private Set<Doctor> doctors = new HashSet<>();

    // Helper methods for managing roles
    public void addRole(Role role) {
        roles.add(role);
        role.getUsers().add(this);
    }

    public void removeRole(Role role) {
        roles.remove(role);
        role.getUsers().remove(this);
    }

    // Get all permissions from all roles
    public Set<Permission> getAllPermissions() {
        Set<Permission> allPermissions = new HashSet<>();
        for (Role role : roles) {
            allPermissions.addAll(role.getPermissions());
        }
        return allPermissions;
    }

    // Helper method to check if user has a specific role
    public boolean hasRole(Role.RoleName roleName) {
        return roles.stream().anyMatch(role -> role.getRoleName() == roleName);
    }

    // Check if user has any of the specified roles
    public boolean hasAnyRole(Role.RoleName... roleNames) {
        for (Role.RoleName roleName : roleNames) {
            if (hasRole(roleName)) {
                return true;
            }
        }
        return false;
    }

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    private Set<UserProfile> profiles = new HashSet<>();

    // Helper methods
    @SuppressWarnings("unchecked")
    public <T extends UserProfile> T getProfile(Class<T> profileType) {
        return profiles.stream()
                .filter(profileType::isInstance)
                .map(profile -> (T) profile)
                .findFirst()
                .orElse(null);
    }

    public <T extends UserProfile> boolean hasProfile(Class<T> profileType) {
        return getProfile(profileType) != null;
    }

    public PatientProfile getPatientProfile() {
        return getProfile(PatientProfile.class);
    }

    public DoctorProfile getDoctorProfile() {
        return getProfile(DoctorProfile.class);
    }

    public LabTechnicianProfile getLabTechnicianProfile() {
        return getProfile(LabTechnicianProfile.class);
    }

    public boolean hasPatientProfile() {
        return hasProfile(PatientProfile.class);
    }

    public boolean hasDoctorProfile() {
        return hasProfile(DoctorProfile.class);
    }

    public boolean hasLabTechnicianProfile() {
        return hasProfile(LabTechnicianProfile.class);
    }

    public void addProfile(UserProfile profile) {
        profiles.add(profile);
        profile.setUser(this);
    }

    public void removeProfile(UserProfile profile) {
        profiles.remove(profile);
        profile.setUser(null);
    }

    // Get the primary profile based on account type
    public UserProfile getPrimaryProfile() {
        return switch (accountType) {
            case PATIENT -> getPatientProfile();
            case DOCTOR -> getDoctorProfile();
            case LAB_TECHNICIAN -> getLabTechnicianProfile();
            default -> null;
        };
    }

    // Get primary profile by profile type (for multi-role support)
    public UserProfile getPrimaryProfile(UserProfile.ProfileType profileType) {
        return switch (profileType) {
            case PATIENT -> getPatientProfile();
            case DOCTOR -> getDoctorProfile();
            case LAB_TECHNICIAN -> getLabTechnicianProfile();
            default -> null;
        };
    }

    // Get all active profiles
    public Set<UserProfile> getActiveProfiles() {
        return profiles.stream()
                .filter(profile -> !profile.isDeleted())
                .collect(java.util.stream.Collectors.toSet());
    }

    // Check if user can create a profile of specific type based on roles
    public boolean canCreateProfile(UserProfile.ProfileType profileType) {
        return switch (profileType) {
            case PATIENT -> hasRole(Role.RoleName.PATIENT);
            case DOCTOR -> hasRole(Role.RoleName.DOCTOR);
            case LAB_TECHNICIAN -> hasRole(Role.RoleName.LAB_TECHNICIAN);
            default -> false;
        };
    }

    public boolean hasPrimaryProfile() {
        return getPrimaryProfile() != null;
    }

    public String getFullName() {
        return getFirstName() + " " + getLastName();
    }

    public enum AccountType {
        DOCTOR,
        PATIENT,
        ADMIN,
        PHARMACY,
        HOSPITAL,
        INSURANCE,
        LAB_TECHNICIAN
    }
}