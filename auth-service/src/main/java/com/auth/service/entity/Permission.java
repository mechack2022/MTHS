package com.auth.service.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "permissions")
@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = "roles")
public class Permission extends BaseEntity {

//    @Id
//    @GeneratedValue(strategy = GenerationType.IDENTITY)
//    private Long id;

//    @Column(name="uuid", nullable = false, unique = true)
//    private String uuid;

    @Column(name = "permission_name", unique = true, nullable = false)
    @Enumerated(EnumType.STRING)
    private PermissionName permissionName;

    @Column(name = "description")
    private String description;

    @Column(name = "resource")
    private String resource; // e.g., "USER", "APPOINTMENT", "PRESCRIPTION"

    @Column(name = "action")
    private String action; // e.g., "CREATE", "READ", "UPDATE", "DELETE"

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

//    @CreationTimestamp
//    @Column(name = "created_at", updatable = false)
//    private LocalDateTime createdAt;
//
//    @UpdateTimestamp
//    @Column(name = "updated_at")
//    private LocalDateTime updatedAt;

    // Many-to-Many relationship with Role (inverse side)
    @ManyToMany(mappedBy = "permissions", fetch = FetchType.LAZY)
    private Set<Role> roles = new HashSet<>();

    // Constructor for easy permission creation
    public Permission(PermissionName permissionName, String description, String resource, String action) {
        this.permissionName = permissionName;
        this.description = description;
        this.resource = resource;
        this.action = action;
    }

    public enum PermissionName {
        // User Management Permissions
        USER_CREATE,
        USER_READ,
        USER_UPDATE,
        USER_DELETE,
        USER_ACTIVATE,
        USER_DEACTIVATE,

        // Role Management Permissions
        ROLE_CREATE,
        ROLE_READ,
        ROLE_UPDATE,
        ROLE_DELETE,
        ROLE_ASSIGN,

        // Permission Management
        PERMISSION_CREATE,
        PERMISSION_READ,
        PERMISSION_UPDATE,
        PERMISSION_DELETE,

        // Patient Management
        PATIENT_CREATE,
        PATIENT_READ,
        PATIENT_UPDATE,
        PATIENT_DELETE,
        PATIENT_VIEW_MEDICAL_HISTORY,

        // Doctor Management
        DOCTOR_CREATE,
        DOCTOR_READ,
        DOCTOR_UPDATE,
        DOCTOR_DELETE,
        DOCTOR_APPROVE,

        // Appointment Management
        APPOINTMENT_CREATE,
        APPOINTMENT_READ,
        APPOINTMENT_UPDATE,
        APPOINTMENT_DELETE,
        APPOINTMENT_CANCEL,
        APPOINTMENT_RESCHEDULE,

        // Prescription Management
        PRESCRIPTION_CREATE,
        PRESCRIPTION_READ,
        PRESCRIPTION_UPDATE,
        PRESCRIPTION_DELETE,
        PRESCRIPTION_APPROVE,

        // Medical Record Management
        MEDICAL_RECORD_CREATE,
        MEDICAL_RECORD_READ,
        MEDICAL_RECORD_UPDATE,
        MEDICAL_RECORD_DELETE,

        // Pharmacy Management
        PHARMACY_CREATE,
        PHARMACY_READ,
        PHARMACY_UPDATE,
        PHARMACY_DELETE,
        PHARMACY_MANAGE_INVENTORY,

        // Hospital Management
        HOSPITAL_CREATE,
        HOSPITAL_READ,
        HOSPITAL_UPDATE,
        HOSPITAL_DELETE,
        HOSPITAL_MANAGE_DEPARTMENTS,

        // Insurance Management
        INSURANCE_CREATE,
        INSURANCE_READ,
        INSURANCE_UPDATE,
        INSURANCE_DELETE,
        INSURANCE_PROCESS_CLAIMS,

        // System Administration
        SYSTEM_ADMIN,
        SYSTEM_CONFIG,
        SYSTEM_MONITOR,
        SYSTEM_BACKUP,

        // Reporting
        REPORT_GENERATE,
        REPORT_VIEW,
        REPORT_EXPORT,

        // Audit
        AUDIT_LOG_READ,
        AUDIT_LOG_EXPORT,

        PROFILE_UPLOAD,
        BIODATA_FILL,
        ACCOUNT_REQUEST_VERIFICATION
    }
}