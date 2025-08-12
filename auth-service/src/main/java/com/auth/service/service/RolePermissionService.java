package com.auth.service.service;

import com.auth.service.entity.Permission;
import com.auth.service.entity.Role;
import com.auth.service.entity.User;
import com.auth.service.repository.PermissionRepository;
import com.auth.service.repository.RoleRepository;
import com.auth.service.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class RolePermissionService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;

    // User Role Management
    public void assignRoleToUser(Long userId, Role.RoleName roleName) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Role role = roleRepository.findByRoleName(roleName)
                .orElseThrow(() -> new RuntimeException("Role not found"));

        user.addRole(role);
        userRepository.save(user);
        log.info("Assigned role {} to user {}", roleName, user.getEmail());
    }

    public void removeRoleFromUser(Long userId, Role.RoleName roleName) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Role role = roleRepository.findByRoleName(roleName)
                .orElseThrow(() -> new RuntimeException("Role not found"));

        user.removeRole(role);
        userRepository.save(user);
        log.info("Removed role {} from user {}", roleName, user.getEmail());
    }

    public void assignRolesToUser(Long userId, Set<Role.RoleName> roleNames) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Set<Role> roles = roleRepository.findByRoleNameIn(roleNames);

        user.getRoles().clear();
        roles.forEach(user::addRole);
        userRepository.save(user);
        log.info("Assigned roles {} to user {}", roleNames, user.getEmail());
    }

    // Role Permission Management
    public void assignPermissionToRole(Role.RoleName roleName, Permission.PermissionName permissionName) {
        Role role = roleRepository.findByRoleName(roleName)
                .orElseThrow(() -> new RuntimeException("Role not found"));

        Permission permission = permissionRepository.findByPermissionName(permissionName)
                .orElseThrow(() -> new RuntimeException("Permission not found"));

        role.addPermission(permission);
        roleRepository.save(role);
        log.info("Assigned permission {} to role {}", permissionName, roleName);
    }

    public void removePermissionFromRole(Role.RoleName roleName, Permission.PermissionName permissionName) {
        Role role = roleRepository.findByRoleName(roleName)
                .orElseThrow(() -> new RuntimeException("Role not found"));

        Permission permission = permissionRepository.findByPermissionName(permissionName)
                .orElseThrow(() -> new RuntimeException("Permission not found"));

        role.removePermission(permission);
        roleRepository.save(role);
        log.info("Removed permission {} from role {}", permissionName, roleName);
    }

    public void assignPermissionsToRole(Role.RoleName roleName, Set<Permission.PermissionName> permissionNames) {
        Role role = roleRepository.findByRoleName(roleName)
                .orElseThrow(() -> new RuntimeException("Role not found"));

        Set<Permission> permissions = permissionRepository.findByPermissionNameIn(permissionNames);

        role.getPermissions().clear();
        permissions.forEach(role::addPermission);
        roleRepository.save(role);
        log.info("Assigned permissions {} to role {}", permissionNames, roleName);
    }

    // Query Methods
    public Set<Role> getUserRoles(Long userId) {
        User user = userRepository.findByIdWithRoles(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return user.getRoles();
    }

    public Set<Permission> getUserPermissions(Long userId) {
        User user = userRepository.findByEmailWithRolesAndPermissions(
                        userRepository.findById(userId)
                                .orElseThrow(() -> new RuntimeException("User not found"))
                                .getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));
        return user.getAllPermissions();
    }

    public Set<Permission> getRolePermissions(Role.RoleName roleName) {
        Role role = roleRepository.findByRoleNameWithPermissions(roleName)
                .orElseThrow(() -> new RuntimeException("Role not found"));
        return role.getPermissions();
    }

    public boolean userHasRole(Long userId, Role.RoleName roleName) {
        User user = userRepository.findByIdWithRoles(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return user.getRoles().stream()
                .anyMatch(role -> role.getRoleName().equals(roleName));
    }

    public boolean userHasPermission(Long userId, Permission.PermissionName permissionName) {
        Set<Permission> userPermissions = getUserPermissions(userId);
        return userPermissions.stream()
                .anyMatch(permission -> permission.getPermissionName().equals(permissionName));
    }

    public boolean userHasAnyPermission(Long userId, Set<Permission.PermissionName> permissionNames) {
        Set<Permission> userPermissions = getUserPermissions(userId);
        return userPermissions.stream()
                .anyMatch(permission -> permissionNames.contains(permission.getPermissionName()));
    }

    public boolean userHasAllPermissions(Long userId, Set<Permission.PermissionName> permissionNames) {
        Set<Permission> userPermissions = getUserPermissions(userId);
        Set<Permission.PermissionName> userPermissionNames = new HashSet<>();
        userPermissions.forEach(permission -> userPermissionNames.add(permission.getPermissionName()));
        return userPermissionNames.containsAll(permissionNames);
    }


}