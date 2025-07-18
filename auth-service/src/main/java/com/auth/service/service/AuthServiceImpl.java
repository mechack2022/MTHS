package com.auth.service.service;


import com.auth.service.config.KeycloakProperties;
import com.auth.service.constants.ApprovalStatus;
import com.auth.service.constants.CodeType;
import com.auth.service.constants.UserType;
import com.auth.service.dto.NewUserRecord;
import com.auth.service.dto.TokenResponse;
import com.auth.service.exceptions.BadRequestException;
import com.auth.service.utils.StringUtils;
import jakarta.ws.rs.core.Response;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.UserResource;
import org.keycloak.admin.client.resource.UsersResource;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
@Slf4j
@AllArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final KeycloakProperties properties;
    private final Keycloak keycloak;
    private final EmailService emailService;
    private final VerificationService verificationService;
    private final RestTemplate restTemplate = new RestTemplate();


    @Override
    public void createUser(NewUserRecord newUserRecord) {
        UserRepresentation userRepresentation = new UserRepresentation();
        userRepresentation.setEnabled(true);
        userRepresentation.setFirstName(newUserRecord.firstName());
        userRepresentation.setLastName(newUserRecord.lastName());
        userRepresentation.setEmail(newUserRecord.email());
        userRepresentation.setUsername(newUserRecord.username());
        userRepresentation.setEmailVerified(false);

        CredentialRepresentation credentialRepresentation = new CredentialRepresentation();
        credentialRepresentation.setValue(newUserRecord.password());
        credentialRepresentation.setType(CredentialRepresentation.PASSWORD);

        userRepresentation.setCredentials(List.of(credentialRepresentation));

        UsersResource userResource = getUsersResource();
        Response response = userResource.create(userRepresentation);
        log.info("Keycloak user creation response status: {}", response.getStatus());

        if (!Objects.equals(201, response.getStatus())) {
            handleKeycloakCreationError(response);
        }

        // Extract userId
        String locationHeader = response.getLocation().getPath();
        String userId = extractUserId(locationHeader);

        // Add custom attributes: RoleType and ApprovalStatus
        UserResource createdUser = userResource.get(userId);
        UserRepresentation userWithAttrs = createdUser.toRepresentation();

        Map<String, List<String>> attributes = new HashMap<>();

        // Convert Set<UserType> to List<String> for attributes
        List<String> roleTypeNames = newUserRecord.userTypes().stream()
                .map(UserType::name)
                .toList();

        attributes.put("roleType", roleTypeNames);
        attributes.put("approvalStatus", List.of(ApprovalStatus.PENDING.name()));

        userWithAttrs.setAttributes(attributes);
        createdUser.update(userWithAttrs);

        // Save verification code
        String code = StringUtils.generateVerificationCode();
        verificationService.saveVerificationCode(userId, code);

        // Send email with code
        emailService.sendVerificationCode(newUserRecord.email(), code);
        log.info("New User created with ID: {}. Verification code sent to: {}", userId, newUserRecord.email());
    }

//
//    @Override
//    public TokenResponse login(String username, String password) {
//        String tokenUrl =  properties.getServerUrl()+"/realms/"+properties.getRealm()+"/protocol/openid-connect/token";
//
//        HttpHeaders headers = new HttpHeaders();
//        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
//
//        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
//        form.add("grant_type", "password");
//        form.add("client_id", properties.getClientId());
//        form.add("client_secret", properties.getClientSecret());
//        form.add("username", username);
//        form.add("password", password);
//
//        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(form, headers);
//
//        ResponseEntity<TokenResponse> response = restTemplate.postForEntity(tokenUrl, request, TokenResponse.class);
//        return response.getBody();
//    }


    private UserRepresentation getUserByUsername(String username) {
        List<UserRepresentation> users = keycloak.realm(properties.getRealm())
                .users()
                .search(username);
        return users.isEmpty() ? null : users.get(0);
    }


    @Override
    public TokenResponse login(String username, String password) {
        // Step 1: Get user by username
        UserRepresentation user = getUserByUsername(username);
        if (user == null) {
            throw new BadRequestException(username, "User not found");
        }

        // Step 2: Check email verification
        if (!Boolean.TRUE.equals(user.isEmailVerified())) {
            throw new BadRequestException("Email", "Email is not verified");
        }

        // Step 3: Check approvalStatus attribute with comprehensive debugging
        Map<String, List<String>> attributes = user.getAttributes();

        // Add comprehensive debugging logs
        log.info("=== DEBUG: User login attempt for username: {} ===", username);
        log.info("User ID: {}", user.getId());
        log.info("User email verified: {}", user.isEmailVerified());
        log.info("User attributes map is null: {}", attributes == null);

        if (attributes != null) {
            log.info("Total attributes count: {}", attributes.size());
            log.info("All attribute keys: {}", attributes.keySet());

            // Check each attribute
            for (Map.Entry<String, List<String>> entry : attributes.entrySet()) {
                log.info("Attribute '{}' -> Values: {}", entry.getKey(), entry.getValue());
            }

            // Specific check for approvalStatus
            if (attributes.containsKey("approvalStatus")) {
                List<String> statusList = attributes.get("approvalStatus");
                log.info("ApprovalStatus list: {}", statusList);
                log.info("ApprovalStatus list size: {}", statusList != null ? statusList.size() : "null");

                if (statusList != null) {
                    for (int i = 0; i < statusList.size(); i++) {
                        String value = statusList.get(i);
                        log.info("ApprovalStatus[{}]: '{}' (length: {})", i, value, value != null ? value.length() : "null");
                    }
                }
            } else {
                log.warn("ApprovalStatus attribute key not found in attributes map");
            }
        }

        String approvalStatus = null;
        if (attributes != null && attributes.containsKey("approvalStatus")) {
            List<String> statusList = attributes.get("approvalStatus");

            if (statusList != null && !statusList.isEmpty()) {
                approvalStatus = statusList.get(0);
                if (approvalStatus != null) {
                    approvalStatus = approvalStatus.trim(); // Remove any whitespace
                }
            }
        }

        log.info("Final processed approvalStatus: '{}' (length: {})",
                approvalStatus, approvalStatus != null ? approvalStatus.length() : "null");
        log.info("APPROVED.equalsIgnoreCase(approvalStatus): {}",
                approvalStatus != null && "APPROVED".equalsIgnoreCase(approvalStatus));
        log.info("=== END DEBUG ===");

        // Check if status is APPROVED (case-insensitive, null-safe)
        if (approvalStatus == null || !"APPROVED".equalsIgnoreCase(approvalStatus)) {
            log.warn("User {} login denied. ApprovalStatus: '{}'", username, approvalStatus);
            throw new BadRequestException(username, "User not approved for login. Current status: " +
                    (approvalStatus != null ? approvalStatus : "NOT_SET"));
        }

        // Step 4: If checks pass, continue to token exchange
        String tokenUrl = properties.getServerUrl() + "/realms/" + properties.getRealm() + "/protocol/openid-connect/token";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("grant_type", "password");
        form.add("client_id", properties.getClientId());
        form.add("client_secret", properties.getClientSecret());
        form.add("username", username);
        form.add("password", password);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(form, headers);

        ResponseEntity<TokenResponse> response = restTemplate.postForEntity(tokenUrl, request, TokenResponse.class);
        return response.getBody();
    }

    @Override
    public void verifyEmailWithCode(String userId, String code) {
        log.info("Starting email verification for userId: {} with provided code", userId);

        try {
            verificationService.validateAndThrowIfInvalid(userId, code, CodeType.EMAIL_VERIFICATION);
            markEmailAsVerified(userId);

            log.info("Email verification completed successfully for userId: {}", userId);

        } catch (BadRequestException e) {
            log.warn("Email verification failed for userId: {} - {}", userId, e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error during email verification for userId: {}", userId, e);
            throw new BadRequestException(
                    "An unexpected error occurred during verification. Please try again.",
                    "VERIFICATION_ERROR"
            );
        }
    }

    @Override
    public void resendVerificationCode(String userId, String email) {
        log.info("Resending verification code for userId: {}", userId);

        try {
            // Check if user exists in Keycloak
            if (!userExistsInKeycloak(userId)) {
                throw new BadRequestException(
                        "User does not exist in the system",
                        "USER_NOT_FOUND"
                );
            }
            // Generate new code
            String newCode = StringUtils.generateVerificationCode();
            // Resend verification code (this invalidates old codes)
            verificationService.resendVerificationCode(userId, newCode, CodeType.EMAIL_VERIFICATION);
            // Send email
            emailService.sendVerificationCode(email, newCode);

            log.info("Verification code resent successfully for userId: {}", userId);

        } catch (BadRequestException e) {
            throw e;
        } catch (Exception e) {
            log.error("Failed to resend verification code for userId: {}", userId, e);
            throw new BadRequestException(
                    "Unable to resend verification code. Please try again later.",
                    "RESEND_FAILED"
            );
        }
    }

    @Override
    public void resendVerificationCode(String email) {
        log.info("Resending verification code for email: {}", email);

        try {
            // Validate email parameter
            if (email == null || email.trim().isEmpty()) {
                throw new BadRequestException(
                        "Email address is required",
                        "INVALID_EMAIL"
                );
            }

            // Find user by email
            UserRepresentation user = getUserByEmail(email);
            if (user == null) {
                throw new BadRequestException(
                        "User does not exist in the system",
                        "USER_NOT_FOUND"
                );
            }

            String userId = user.getId();

            // Generate new code
            String newCode = StringUtils.generateVerificationCode();

            // Resend verification code (this invalidates old codes)
            verificationService.resendVerificationCode(userId, newCode, CodeType.EMAIL_VERIFICATION);

            // Send email
            emailService.sendVerificationCode(email, newCode);

            log.info("Verification code resent successfully for userId: {}", userId);

        } catch (BadRequestException e) {
            log.warn("Failed to resend verification code for email: {} - {}", email, e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error while resending verification code for email: {}", email, e);
            throw new BadRequestException(
                    "Unable to resend verification code. Please try again later.",
                    "RESEND_FAILED"
            );
        }
    }

    // Make sure this method is properly implemented
    private UserRepresentation getUserByEmail(String email) {
        try {
            List<UserRepresentation> users = keycloak.realm(properties.getRealm())
                    .users()
                    .search(null, null, null, email, 0, 1);

            if (users != null && !users.isEmpty()) {
                // Verify email matches exactly (case-insensitive)
                for (UserRepresentation user : users) {
                    if (email.equalsIgnoreCase(user.getEmail())) {
                        return user;
                    }
                }
            }

            return null;
        } catch (Exception e) {
            log.error("Error searching for user by email: {}", email, e);
            throw new BadRequestException(
                    "Error searching for user in the system",
                    "USER_SEARCH_FAILED"
            );
        }
    }


    @Override
    public void initiatePasswordReset(String email) {
        log.info("Initiating password reset for email: {}", email);

        try {
            // Find user by email in Keycloak
            String userId = findUserIdByEmail(email);

            if (userId == null) {
                // For security reasons, don't reveal if email exists or not
                // Just log it and return success to prevent email enumeration
                log.warn("Password reset requested for non-existent email: {}", email);
                return; // Still return success to prevent email enumeration
            }

            // Generate password reset code
            String resetCode = StringUtils.generateVerificationCode();

            // Save the code to database with PASSWORD_RESET type
            verificationService.saveVerificationCode(userId, resetCode, CodeType.PASSWORD_RESET);

            // Send password reset email
            emailService.sendPasswordResetCode(email, resetCode, CodeType.PASSWORD_RESET);

            log.info("Password reset code sent successfully for email: {}", email);

        } catch (Exception e) {
            log.error("Failed to initiate password reset for email: {}", email, e);
            throw new BadRequestException(
                    "Unable to process password reset request. Please try again later.",
                    "PASSWORD_RESET_FAILED"
            );
        }
    }

    @Override
    public void verifyResetPasswordCode(String email, String code) {
        log.info("Processing password reset for email: {} with provided code", email);

        try {
            // Find user by email
            String userId = findUserIdByEmail(email);

            if (userId == null) {
                throw new BadRequestException(
                        "Invalid email address",
                        "INVALID_EMAIL"
                );
            }

            // Validate the password reset code
            verificationService.validateAndThrowIfInvalid(userId, code, CodeType.PASSWORD_RESET);

            // Update password in Keycloak
//            updatePasswordInKeycloak(userId, newPassword);

            // Invalidate the used code
            verificationService.invalidateCode(userId, code, CodeType.PASSWORD_RESET);

            log.info("Password reset completed successfully for email: {}", email);

        } catch (BadRequestException e) {
            log.warn("Password reset failed for email: {} - {}", email, e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error during password reset for email: {}", email, e);
            throw new BadRequestException(
                    "An unexpected error occurred during password reset. Please try again.",
                    "PASSWORD_RESET_ERROR"
            );
        }
    }

    @Override
    public void resendPasswordResetCode(String email) {
        log.info("Resending password reset code for email: {}", email);

        try {
            // Find user by email
            String userId = findUserIdByEmail(email);

            if (userId == null) {
                // For security reasons, don't reveal if email exists or not
                log.warn("Password reset code resend requested for non-existent email: {}", email);
                return; // Still return success to prevent email enumeration
            }

            // Generate new password reset code
            String newCode = StringUtils.generateVerificationCode();

            // Resend password reset code (this invalidates old codes)
            verificationService.resendVerificationCode(userId, newCode, CodeType.PASSWORD_RESET);

            // Send email
            emailService.sendVerificationCode(email, newCode);

            log.info("Password reset code resent successfully for email: {}", email);

        } catch (Exception e) {
            log.error("Failed to resend password reset code for email: {}", email, e);
            throw new BadRequestException(
                    "Unable to resend password reset code. Please try again later.",
                    "RESEND_FAILED"
            );
        }
    }

    @Override
    public void resetPassword(String userId, String newPassword, String confirmPassword) {
        if (!newPassword.equals(confirmPassword)) {
            throw new BadRequestException(
                    "Password and confirm password do not match",
                    "PASSWORD_MISMATCH"
            );
        }

        if (newPassword == null || newPassword.trim().isEmpty()) {
            throw new BadRequestException(
                    "Password cannot be empty",
                    "EMPTY_PASSWORD"
            );
        }

        try {
            var userResource = keycloak.realm(properties.getRealm()).users().get(userId);

            // Verify user exists by trying to get representation
            userResource.toRepresentation();

            // Create new credential representation
            CredentialRepresentation credentialRepresentation = new CredentialRepresentation();
            credentialRepresentation.setType(CredentialRepresentation.PASSWORD);
            credentialRepresentation.setValue(newPassword);
            credentialRepresentation.setTemporary(false);

            // Update the password
            userResource.resetPassword(credentialRepresentation);

            log.info("Password updated successfully in Keycloak for userId: {}", userId);

        } catch (BadRequestException e) {
            // Re-throw our custom validation exceptions
            throw e;
        } catch (Exception e) {
            String errorMessage = e.getMessage();
            log.error("Failed to update password in Keycloak for userId: {}", userId, e);

            if (errorMessage != null) {
                if (errorMessage.contains("404") || errorMessage.contains("Not Found")) {
                    throw new BadRequestException(
                            "User does not exist in the system",
                            "USER_NOT_FOUND"
                    );
                } else if (errorMessage.contains("403") || errorMessage.contains("Forbidden")) {
                    throw new BadRequestException(
                            "Insufficient permissions to update password",
                            "PERMISSION_DENIED"
                    );
                } else if (errorMessage.toLowerCase().contains("password") &&
                        (errorMessage.toLowerCase().contains("policy") ||
                                errorMessage.toLowerCase().contains("constraint") ||
                                errorMessage.toLowerCase().contains("requirement"))) {
                    throw new BadRequestException(
                            "New password does not meet the required password policy. Please ensure your password meets all security requirements.",
                            "PASSWORD_POLICY_VIOLATION"
                    );
                }
            }

            throw new BadRequestException(
                    "Failed to update password. Please try again.",
                    "PASSWORD_UPDATE_FAILED"
            );
        }
    }

    private String findUserIdByEmail(String email) {
        try {
            UsersResource usersResource = getUsersResource();
            List<UserRepresentation> users = usersResource.search(null, null, null, email, 0, 1);

            if (users != null && !users.isEmpty()) {
                UserRepresentation user = users.get(0);
                // Double-check email matches exactly (case-insensitive)
                if (email.equalsIgnoreCase(user.getEmail())) {
                    return user.getId();
                }
            }

            return null;
        } catch (Exception e) {
            log.error("Error searching for user by email: {}", email, e);
            return null;
        }
    }

    private UsersResource getUsersResource() {
        return keycloak.realm(properties.getRealm()).users();
    }

    private String extractUserId(String locationHeader) {
        if (locationHeader == null || !locationHeader.contains("/")) {
            throw new IllegalArgumentException("Invalid Location header: " + locationHeader);
        }
        return locationHeader.substring(locationHeader.lastIndexOf("/") + 1);
    }

    private boolean userExistsInKeycloak(String userId) {
        try {
            var userResource = keycloak.realm(properties.getRealm()).users().get(userId);
            userResource.toRepresentation();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private void handleKeycloakCreationError(Response response) {
        int status = response.getStatus();
        String responseBody = "";

        try {
            if (response.hasEntity()) {
                responseBody = response.readEntity(String.class);
            }
        } catch (Exception e) {
            log.warn("Could not read response body", e);
        }

        log.error("Keycloak user creation failed with status: {}, body: {}", status, responseBody);

        switch (status) {
            case 409:
                if (responseBody.toLowerCase().contains("username")) {
                    throw new BadRequestException(
                            "A user with this username already exists. Please choose a different username.",
                            "USERNAME_EXISTS"
                    );
                } else if (responseBody.toLowerCase().contains("email")) {
                    throw new BadRequestException(
                            "A user with this email already exists. Please use a different email address.",
                            "EMAIL_EXISTS"
                    );
                } else {
                    throw new BadRequestException(
                            "A user with these details already exists.",
                            "USER_EXISTS"
                    );
                }
            case 400:
                throw new BadRequestException(
                        "The provided user data is invalid. Please check all fields and try again.",
                        "INVALID_USER_DATA"
                );
            case 403:
                throw new BadRequestException(
                        "Insufficient permissions to create user.",
                        "PERMISSION_DENIED"
                );
            default:
                throw new RuntimeException("Failed to create user in Keycloak. Status: " + status);
        }
    }

    @Override
    public void markEmailAsVerified(String userId) {
        log.info("Starting email verification process for userId: {}", userId);

        try {
            var realmResource = keycloak.realm(properties.getRealm());
            var userResource = realmResource.users().get(userId);

            UserRepresentation user = userResource.toRepresentation();
            log.debug("Current email verified status for userId {}: {}", userId, user.isEmailVerified());

            // Set email as verified
            user.setEmailVerified(true);
            userResource.update(user);

            log.info("Email marked as verified for userId: {}", userId);

            // Verify the update was successful
            UserRepresentation updatedUser = userResource.toRepresentation();
            log.info("Updated user email verified status: {}", updatedUser.isEmailVerified());

        } catch (Exception e) {
            String errorMessage = e.getMessage();
            log.error("Failed to mark email as verified for userId: {}", userId, e);

            if (errorMessage != null) {
                if (errorMessage.contains("404") || errorMessage.contains("Not Found")) {
                    throw new BadRequestException(
                            "User does not exist in Keycloak: " + userId,
                            "USER_NOT_FOUND"
                    );
                } else if (errorMessage.contains("403") || errorMessage.contains("Forbidden")) {
                    throw new BadRequestException(
                            "Insufficient permissions to update user in Keycloak",
                            "PERMISSION_DENIED"
                    );
                }
            }

            throw new BadRequestException(
                    "Failed to update email verification status in Keycloak",
                    "KEYCLOAK_UPDATE_FAILED"
            );
        }
    }
}
