package com.auth.service.service;

import com.auth.service.entity.Permission;
import com.auth.service.entity.Role;
import com.auth.service.repository.PermissionRepository;
import com.auth.service.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import java.util.regex.Pattern;
import java.util.regex.Matcher;

@Service
@RequiredArgsConstructor
@Slf4j
public class DataInitializationService implements CommandLineRunner {

    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;

    @Override
    @Transactional
    public void run(String... args) {
        initializePermissions();
        initializeRoles();
        assignPermissionsToRoles();
        log.info("Data initialization completed");
    }

    private void initializePermissions() {
        LocalDateTime now = LocalDateTime.now();
        for (Permission.PermissionName permissionName : Permission.PermissionName.values()) {
            if (!permissionRepository.existsByPermissionName(permissionName)) {
                Permission permission = new Permission();
                permission.setPermissionName(permissionName);
                permission.setDescription(generateDescription(permissionName.name()));

                String[] parts = permissionName.name().split("_");
                if (parts.length >= 2) {
                    permission.setResource(parts[0]);
                    permission.setAction(parts[1]);
                }
                // Manually set timestamps
//                permission.setCreatedAt(now);
//                permission.setUpdatedAt(now);
                permissionRepository.save(permission);
                log.debug("Created permission: {}", permissionName);
            }
        }
    }

    private void initializeRoles() {
        for (Role.RoleName roleName : Role.RoleName.values()) {
            if (!roleRepository.existsByRoleName(roleName)) {
                Role role = new Role();
                role.setRoleName(roleName);
                role.setDescription(generateDescription(roleName.name()));
                roleRepository.save(role);
                log.debug("Created role: {}", roleName);
            }
        }
    }

    private void assignPermissionsToRoles() {
        // Super Admin - All permissions
        assignPermissionsToRole(Role.RoleName.SUPER_ADMIN, Arrays.asList(Permission.PermissionName.values()));

        // Admin - Most permissions except super admin specific
        assignPermissionsToRole(Role.RoleName.ADMIN, Arrays.asList(
                Permission.PermissionName.USER_CREATE, Permission.PermissionName.USER_READ,
                Permission.PermissionName.USER_UPDATE, Permission.PermissionName.USER_DELETE,
                Permission.PermissionName.ROLE_READ, Permission.PermissionName.ROLE_ASSIGN,
                Permission.PermissionName.DOCTOR_CREATE, Permission.PermissionName.DOCTOR_READ,
                Permission.PermissionName.DOCTOR_UPDATE, Permission.PermissionName.DOCTOR_APPROVE,
                Permission.PermissionName.PATIENT_CREATE, Permission.PermissionName.PATIENT_READ,
                Permission.PermissionName.PATIENT_UPDATE,
                Permission.PermissionName.APPOINTMENT_READ, Permission.PermissionName.APPOINTMENT_UPDATE,
                Permission.PermissionName.REPORT_GENERATE, Permission.PermissionName.REPORT_VIEW,
                Permission.PermissionName.AUDIT_LOG_READ
        ));

        // Doctor permissions
        assignPermissionsToRole(Role.RoleName.DOCTOR, Arrays.asList(
                Permission.PermissionName.PATIENT_READ, Permission.PermissionName.PATIENT_VIEW_MEDICAL_HISTORY,
                Permission.PermissionName.APPOINTMENT_READ, Permission.PermissionName.APPOINTMENT_UPDATE,
                Permission.PermissionName.APPOINTMENT_CANCEL, Permission.PermissionName.APPOINTMENT_RESCHEDULE,
                Permission.PermissionName.PRESCRIPTION_CREATE, Permission.PermissionName.PRESCRIPTION_READ,
                Permission.PermissionName.PRESCRIPTION_UPDATE,
                Permission.PermissionName.MEDICAL_RECORD_CREATE, Permission.PermissionName.MEDICAL_RECORD_READ,
                Permission.PermissionName.MEDICAL_RECORD_UPDATE
        ));

        // Patient permissions
        assignPermissionsToRole(Role.RoleName.PATIENT, Arrays.asList(
                Permission.PermissionName.APPOINTMENT_CREATE, Permission.PermissionName.APPOINTMENT_READ,
                Permission.PermissionName.APPOINTMENT_CANCEL, Permission.PermissionName.APPOINTMENT_RESCHEDULE,
                Permission.PermissionName.PRESCRIPTION_READ,
                Permission.PermissionName.MEDICAL_RECORD_READ
        ));

        assignPermissionsToRole(Role.RoleName.PENDING, Arrays.asList(
                Permission.PermissionName.PROFILE_UPLOAD,
                Permission.PermissionName.BIODATA_FILL,
                Permission.PermissionName.ACCOUNT_REQUEST_VERIFICATION
        ));


        // Hospital Admin permissions
        assignPermissionsToRole(Role.RoleName.HOSPITAL_ADMIN, Arrays.asList(
                Permission.PermissionName.HOSPITAL_READ, Permission.PermissionName.HOSPITAL_UPDATE,
                Permission.PermissionName.HOSPITAL_MANAGE_DEPARTMENTS,
                Permission.PermissionName.DOCTOR_CREATE, Permission.PermissionName.DOCTOR_READ,
                Permission.PermissionName.DOCTOR_UPDATE, Permission.PermissionName.DOCTOR_APPROVE,
                Permission.PermissionName.APPOINTMENT_READ, Permission.PermissionName.APPOINTMENT_UPDATE,
                Permission.PermissionName.REPORT_GENERATE, Permission.PermissionName.REPORT_VIEW
        ));

        // Pharmacy Admin permissions
        assignPermissionsToRole(Role.RoleName.PHARMACY_ADMIN, Arrays.asList(
                Permission.PermissionName.PHARMACY_READ, Permission.PermissionName.PHARMACY_UPDATE,
                Permission.PermissionName.PHARMACY_MANAGE_INVENTORY,
                Permission.PermissionName.PRESCRIPTION_READ, Permission.PermissionName.PRESCRIPTION_UPDATE
        ));

        // User role - basic permissions
        assignPermissionsToRole(Role.RoleName.USER, Arrays.asList(
                Permission.PermissionName.USER_READ
        ));
    }

    private void assignPermissionsToRole(Role.RoleName roleName, java.util.List<Permission.PermissionName> permissionNames) {
        Role role = roleRepository.findByRoleName(roleName).orElse(null);
        if (role == null) {
            log.warn("Role not found: {}", roleName);
            return;
        }

        Set<Permission> permissions = new HashSet<>();
        for (Permission.PermissionName permissionName : permissionNames) {
            Permission permission = permissionRepository.findByPermissionName(permissionName).orElse(null);
            if (permission != null) {
                permissions.add(permission);
            } else {
                log.warn("Permission not found: {}", permissionName);
            }
        }

        role.setPermissions(permissions);
        roleRepository.save(role);
        log.debug("Assigned {} permissions to role {}", permissions.size(), roleName);
    }

    private String generateDescription(String name) {
        return Pattern.compile("\\b\\w")
                .matcher(name.replace("_", " ").toLowerCase())
                .replaceAll(m -> m.group().toUpperCase());
    }
}