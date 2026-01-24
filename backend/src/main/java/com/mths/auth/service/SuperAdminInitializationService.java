package com.mths.auth.service;

import com.mths.auth.entity.Role;
import com.mths.auth.entity.User;
import com.mths.auth.entity.Permission;
import com.mths.auth.repository.UserRepository;
import com.mths.auth.repository.RoleRepository;
import com.mths.auth.repository.PermissionRepository;
import com.mths.shared.constants.VerificationStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Order(1) // Execute before other initialization services
public class SuperAdminInitializationService implements CommandLineRunner {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.superadmin.email:superadmin@healthcare.com}")
    private String superAdminEmail;

    @Value("${app.superadmin.password}")
    private String superAdminPassword;

    @Value("${app.superadmin.firstName:Super}")
    private String superAdminFirstName;

    @Value("${app.superadmin.lastName:Administrator}")
    private String superAdminLastName;

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        log.info("Starting Super Admin initialization...");
        
        try {
            initializeSuperAdmin();
            log.info("Super Admin initialization completed successfully");
        } catch (Exception e) {
            log.error("Failed to initialize Super Admin: {}", e.getMessage(), e);
            throw e;
        }
    }

    @Transactional
    public void initializeSuperAdmin() {
        // Check if Super Admin already exists
        Optional<User> existingSuperAdmin = userRepository.findByEmail(superAdminEmail);
        if (existingSuperAdmin.isPresent()) {
            log.info("Super Admin already exists with email: {}", superAdminEmail);
            User superAdmin = existingSuperAdmin.get();
            
            // Ensure super admin has all required properties
            ensureSuperAdminProperties(superAdmin);
            return;
        }

        log.info("Creating Super Admin user...");

        // Get or create SUPER_ADMIN role
        Role superAdminRole = getOrCreateSuperAdminRole();
        
        // Create Super Admin user
        User superAdmin = createSuperAdminUser(superAdminRole);
        
        log.info("Super Admin created successfully with email: {}", superAdminEmail);
        log.info("Super Admin default credentials - Email: {}, Password: {}", superAdminEmail, superAdminPassword);
        log.warn("IMPORTANT: Please change the Super Admin password after first login!");
    }

    private Role getOrCreateSuperAdminRole() {
        Optional<Role> existingRole = roleRepository.findByRoleName(Role.RoleName.SUPER_ADMIN);
        if (existingRole.isPresent()) {
            Role role = existingRole.get();
            // Ensure role has all permissions
            ensureRoleHasAllPermissions(role);
            return role;
        }

        // Create SUPER_ADMIN role with all permissions
        Role superAdminRole = new Role();
        superAdminRole.setRoleName(Role.RoleName.SUPER_ADMIN);
        superAdminRole.setDescription("Super Administrator with all system permissions");
        superAdminRole.setCreatedAt(LocalDateTime.now());
        superAdminRole.setUpdatedAt(LocalDateTime.now());

        // Get all permissions and assign to super admin role
        Set<Permission> allPermissions = new HashSet<>(permissionRepository.findAll());
        superAdminRole.setPermissions(allPermissions);

        Role savedRole = roleRepository.save(superAdminRole);
        log.info("Created SUPER_ADMIN role with {} permissions", allPermissions.size());
        
        return savedRole;
    }

    private void ensureRoleHasAllPermissions(Role role) {
        Set<Permission> allPermissions = new HashSet<>(permissionRepository.findAll());
        Set<Permission> currentPermissions = role.getPermissions();
        
        if (currentPermissions.size() != allPermissions.size()) {
            role.setPermissions(allPermissions);
            roleRepository.save(role);
            log.info("Updated SUPER_ADMIN role permissions: {} -> {}", 
                    currentPermissions.size(), allPermissions.size());
        }
    }

    private User createSuperAdminUser(Role superAdminRole) {
        User superAdmin = new User();
        superAdmin.setUuid(UUID.randomUUID().toString());
        superAdmin.setEmail(superAdminEmail);
        superAdmin.setPassword(passwordEncoder.encode(superAdminPassword));
        superAdmin.setFirstName(superAdminFirstName);
        superAdmin.setLastName(superAdminLastName);
        superAdmin.setMailVerified(true); // Super admin is pre-verified
        superAdmin.setAccountVerified(true); // Super admin is pre-verified
        superAdmin.setVerificationStatus(VerificationStatus.APPROVED);
        superAdmin.setVerifiedBy("SYSTEM");
        superAdmin.setVerificationDate(LocalDateTime.now());
        superAdmin.setIsActive(true);
        superAdmin.setAccountType(User.AccountType.ADMIN);
        superAdmin.setCreatedAt(LocalDateTime.now());
        superAdmin.setUpdatedAt(LocalDateTime.now());

        // Add SUPER_ADMIN role
        Set<Role> roles = new HashSet<>();
        roles.add(superAdminRole);
        superAdmin.setRoles(roles);

        User savedUser = userRepository.save(superAdmin);
        log.info("Created Super Admin user: {} ({})", savedUser.getEmail(), savedUser.getUuid());
        
        return savedUser;
    }

    private void ensureSuperAdminProperties(User superAdmin) {
        boolean updated = false;
        
        // Ensure super admin is verified
        if (!superAdmin.getAccountVerified()) {
            superAdmin.setAccountVerified(true);
            superAdmin.setVerificationStatus(VerificationStatus.APPROVED);
            superAdmin.setVerifiedBy("SYSTEM");
            superAdmin.setVerificationDate(LocalDateTime.now());
            updated = true;
        }
        
        // Ensure super admin is active
        if (!superAdmin.getIsActive()) {
            superAdmin.setIsActive(true);
            updated = true;
        }
        
        // Ensure super admin has email verified
        if (!superAdmin.getMailVerified()) {
            superAdmin.setMailVerified(true);
            updated = true;
        }
        
        // Ensure super admin has SUPER_ADMIN role
        boolean hasSuperAdminRole = superAdmin.getRoles().stream()
            .anyMatch(role -> role.getRoleName() == Role.RoleName.SUPER_ADMIN);
        
        if (!hasSuperAdminRole) {
            Role superAdminRole = getOrCreateSuperAdminRole();
            superAdmin.getRoles().add(superAdminRole);
            updated = true;
        }
        
        if (updated) {
            superAdmin.setUpdatedAt(LocalDateTime.now());
            userRepository.save(superAdmin);
            log.info("Updated existing Super Admin properties");
        }
    }
}