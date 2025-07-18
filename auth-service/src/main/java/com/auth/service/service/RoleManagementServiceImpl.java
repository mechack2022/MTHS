//package com.auth.service.service;
//
//import com.auth.service.config.KeycloakProperties;
//import com.auth.service.constants.UserType;
//import lombok.AllArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.keycloak.admin.client.Keycloak;
//import org.keycloak.admin.client.resource.RealmResource;
//import org.keycloak.admin.client.resource.RoleMappingResource;
//import org.keycloak.admin.client.resource.RolesResource;
//import org.keycloak.admin.client.resource.UserResource;
//import org.keycloak.representations.idm.RoleRepresentation;
//import org.springframework.stereotype.Service;
//
//import java.util.ArrayList;
//import java.util.List;
//import java.util.Set;
//import java.util.stream.Collectors;
//
//@Service
//@Slf4j
//@AllArgsConstructor
//public class RoleManagementServiceImpl {
//
//    private final Keycloak keycloak;
//
//    private final KeycloakProperties properties;
//
//    private final String realm = properties.getRealm();
//
//    public void assignRolesToUser(String userId, Set<UserType> userTypes) {
//        try {
//            UserResource userResource = keycloak.realm(realm).users().get(userId);
//            RoleMappingResource roleMappingResource = userResource.roles();
//
//            // Get realm roles
//            RealmResource realmResource = keycloak.realm(realm);
//            RolesResource rolesResource = realmResource.roles();
//
//            List<RoleRepresentation> rolesToAssign = new ArrayList<>();
//
//            for (UserType userType : userTypes) {
//                try {
//                    RoleRepresentation role = rolesResource.get(userType.name()).toRepresentation();
//                    rolesToAssign.add(role);
//                    log.info("Adding role {} to user {}", userType.name(), userId);
//                } catch (Exception e) {
//                    log.warn("Role {} not found in Keycloak, skipping", userType.name());
//                }
//            }
//
//            if (!rolesToAssign.isEmpty()) {
//                roleMappingResource.realmLevel().add(rolesToAssign);
//                log.info("Successfully assigned {} roles to user {}", rolesToAssign.size(), userId);
//            }
//
//        } catch (Exception e) {
//            log.error("Failed to assign roles to user {}: {}", userId, e.getMessage());
//            throw new RuntimeException("Failed to assign roles to user", e);
//        }
//    }
//
//    public void removeRoleFromUser(String userId, UserType userType) {
//        try {
//            UserResource userResource = keycloak.realm(realm).users().get(userId);
//            RoleMappingResource roleMappingResource = userResource.roles();
//
//            RealmResource realmResource = keycloak.realm(realm);
//            RoleRepresentation role = realmResource.roles().get(userType.name()).toRepresentation();
//
//            roleMappingResource.realmLevel().remove(List.of(role));
//            log.info("Removed role {} from user {}", userType.name(), userId);
//
//        } catch (Exception e) {
//            log.error("Failed to remove role {} from user {}: {}", userType.name(), userId, e.getMessage());
//            throw new RuntimeException("Failed to remove role from user", e);
//        }
//    }
//
//    public Set<UserType> getUserRoles(String userId) {
//        try {
//            UserResource userResource = keycloak.realm(realm).users().get(userId);
//            List<RoleRepresentation> realmRoles = userResource.roles().realmLevel().listEffective();
//
//            return realmRoles.stream()
//                    .map(RoleRepresentation::getName)
//                    .filter(roleName -> {
//                        try {
//                            UserType.valueOf(roleName);
//                            return true;
//                        } catch (IllegalArgumentException e) {
//                            return false;
//                        }
//                    })
//                    .map(UserType::valueOf)
//                    .collect(Collectors.toSet());
//
//        } catch (Exception e) {
//            log.error("Failed to get roles for user {}: {}", userId, e.getMessage());
//            return Set.of();
//        }
//    }
//
//    public void ensureRolesExist() {
//        try {
//            RolesResource rolesResource = keycloak.realm(realm).roles();
//
//            for (UserType userType : UserType.values()) {
//                try {
//                    rolesResource.get(userType.name()).toRepresentation();
//                    log.debug("Role {} already exists", userType.name());
//                } catch (Exception e) {
//                    // Role doesn't exist, create it
//                    RoleRepresentation role = new RoleRepresentation();
//                    role.setName(userType.name());
//                    role.setDescription("Role for " + userType.name());
//                    rolesResource.create(role);
//                    log.info("Created role: {}", userType.name());
//                }
//            }
//        } catch (Exception e) {
//            log.error("Error ensuring roles exist: {}", e.getMessage());
//        }
//    }
//}
