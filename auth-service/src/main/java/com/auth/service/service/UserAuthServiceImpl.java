package com.auth.service.service;

import com.auth.service.dto.TokenResponse;
import com.auth.service.dto.UserDTO;
import com.auth.service.entity.*;
import com.auth.service.repository.PasswordResetTokenRepository;
import com.auth.service.exceptions.BadRequestException;
import com.auth.service.exceptions.ResourceNotFoundException;
import com.auth.service.mapper.UserMapper;
import com.auth.service.repository.UserRepository;
import com.auth.service.repository.PasswordResetRepository;
import com.auth.service.repository.RoleRepository;
import com.auth.service.constants.CodeType;
import com.auth.service.constants.VerificationResult;
import com.auth.service.jwt.JwtTokenProvider;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional
@Slf4j
public class UserAuthServiceImpl implements UserAuthService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordResetRepository passwordResetRepository;

    @Autowired
    private PasswordResetTokenRepository passwordResetTokenRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private VerificationService verificationService;

    @Autowired
    private CodeGeneratorService codeGeneratorService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private EmailService emailService;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private RolePermissionService rolePermissionService;

    // ========================================================================
    // USER REGISTRATION & AUTHENTICATION - CORE RESPONSIBILITY
    // ========================================================================

    @Override
    public UserDTO createUser(UserDTO req) {
        // Check if user already exists
        if (userRepository.existsByEmail(req.getEmail())) {
            throw new BadRequestException("email", req.getEmail() + " has been taken");
        }

        // Create new user - NO PROFILE CREATION
        User user = new User();
        user.setUuid(UUID.randomUUID().toString());
        user.setEmail(req.getEmail());
        user.setPassword(passwordEncoder.encode(req.getPassword()));
        user.setFirstName(req.getFirstName());
        user.setLastName(req.getLastName());
        user.setMailVerified(false);
        user.setAccountVerified(false); // Will be true when profile is complete
        user.setIsActive(true);
        user.setAccountType(req.getAccountType());
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());

        // Save user
        User savedUser = userRepository.save(user);

        // Assign initial PENDING role - will be upgraded by ProfileService when profile is complete
        assignInitialRole(savedUser, req.getAccountType());

        // Generate and save verification code
        String verificationCode = codeGeneratorService.generateVerificationCode(5);
        verificationService.saveVerificationCode(savedUser.getUuid(),
                passwordEncoder.encode(verificationCode),
                CodeType.EMAIL_VERIFICATION);

        // Send verification email
        emailService.sendEmail(savedUser.getEmail(), verificationCode, CodeType.EMAIL_VERIFICATION);

        return userMapper.toDto(savedUser);
    }

    @Override
    public TokenResponse login(String username, String password) {
        // Find user by email
        Optional<User> userOpt = userRepository.findByEmail(username);

        if (userOpt.isEmpty()) {
            throw new BadRequestException("username or password", "Invalid email or password");
        }

        User user = userOpt.get();

        // Check if user is active
        if (!user.getIsActive()) {
            throw new BadRequestException("account", "Account is inactive. Please contact support.");
        }

        // Verify password
        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new IllegalArgumentException("Invalid email or password");
        }

        // Check if email is verified
        if (!user.getMailVerified()) {
            throw new RuntimeException("Email not verified. Please verify your email first.");
        }

        // Generate tokens
        String newAccessToken = jwtTokenProvider.generateToken(username);
        String newRefreshToken = jwtTokenProvider.generateRefreshToken(username);

        // Update user timestamps
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);

        return new TokenResponse(
                newAccessToken,
                newRefreshToken,
                (int) jwtTokenProvider.getTokenExpiration()
        );
    }

    @Override
    public void logout(String refreshToken) {
        try {
            if (jwtTokenProvider.validateToken(refreshToken) && jwtTokenProvider.isRefreshToken(refreshToken)) {
                String username = jwtTokenProvider.getUsername(refreshToken);
                Optional<User> userOpt = userRepository.findByEmail(username);

                if (userOpt.isPresent()) {
                    User user = userOpt.get();
                    user.setUpdatedAt(LocalDateTime.now());
                    userRepository.save(user);
                }
            }
        } catch (Exception e) {
            System.err.println("Error during logout: " + e.getMessage());
        }
    }

    // ========================================================================
    // EMAIL VERIFICATION - AUTHENTICATION RESPONSIBILITY
    // ========================================================================

    @Override
    public VerificationResult verifyEmailWithCode(String userId, String code) {
        User user = findUserByUuid(userId);

        if (user.getMailVerified()) {
            throw new BadRequestException(user.getEmail(), "Email is already verified");
        }

        // Validate the code
        VerificationResult verificationResult = verificationService.validateAndThrowIfInvalid(
                userId, code, CodeType.EMAIL_VERIFICATION
        );

        // Mark email as verified
        user.setMailVerified(true);
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);

        return verificationResult;
    }

    // Old method removed - use resendEmailVerificationCode instead

    @Override
    public String resendEmailVerificationCode(String email) {
        Optional<User> userOpt = userRepository.findByEmail(email);
        
        // Use same response for security (don't reveal if email exists)
        if (userOpt.isEmpty()) {
            log.warn("Resend verification code requested for non-existent email: {}", email);
            return "If an account with this email exists and is not verified, a new verification code has been sent.";
        }
        
        User user = userOpt.get();
        
        if (Boolean.TRUE.equals(user.getMailVerified())) {
            log.info("Resend verification code requested for already verified email: {}", email);
            return "If an account with this email exists and is not verified, a new verification code has been sent.";
        }

        // Check rate limiting
        if (!verificationService.canRequestNewCode(user.getUuid(), CodeType.EMAIL_VERIFICATION)) {
            long remainingSeconds = verificationService.getRemainingCooldownSeconds(user.getUuid(), CodeType.EMAIL_VERIFICATION);
            throw new BadRequestException("rate_limit", 
                String.format("Please wait %d seconds before requesting a new verification code.", remainingSeconds));
        }

        sendNewVerificationCode(user);
        log.info("Email verification code resent to: {}", email);
        return "If an account with this email exists and is not verified, a new verification code has been sent.";
    }

    @Override
    public void markEmailAsVerified(String userId) {
        User user = findUserByUuid(userId);

        if (user.getMailVerified()) {
            return; // Already verified
        }

        user.setMailVerified(true);
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);

        // Invalidate all email verification codes for this user
        verificationService.invalidateExistingCodes(userId, CodeType.EMAIL_VERIFICATION);
    }

    // ========================================================================
    // PASSWORD RESET - AUTHENTICATION RESPONSIBILITY
    // ========================================================================

    @Override
    public void initiatePasswordReset(String email) {
        Optional<User> userOpt = userRepository.findByEmail(email);
        if (userOpt.isEmpty()) {
            // Don't reveal if email exists or not for security - but still log for monitoring
            log.warn("Password reset requested for non-existent email: {}", email);
            return;
        }
        
        User user = userOpt.get();

        // Check rate limiting
        if (!verificationService.canRequestNewCode(user.getUuid(), CodeType.PASSWORD_RESET)) {
            long remainingSeconds = verificationService.getRemainingCooldownSeconds(user.getUuid(), CodeType.PASSWORD_RESET);
            throw new BadRequestException("rate_limit", 
                String.format("Please wait %d seconds before requesting a new password reset code.", remainingSeconds));
        }

        // Invalidate any existing password reset tokens
        passwordResetTokenRepository.invalidateAllTokensForUser(user.getUuid());

        // Generate password reset code
        String resetCode = codeGeneratorService.generateAlphanumericCode(6);

        // Save verification code (hashed)
        verificationService.saveVerificationCode(user.getUuid(),
                passwordEncoder.encode(resetCode),
                CodeType.PASSWORD_RESET);

        // Send password reset email
        emailService.sendEmail(user.getEmail(), resetCode, CodeType.PASSWORD_RESET);
        
        log.info("Password reset initiated for email: {}", email);
    }

    @Override
    public String verifyResetPasswordCode(String email, String code) {
        Optional<User> userOpt = userRepository.findByEmail(email);
        if (userOpt.isEmpty()) {
            throw new BadRequestException("email", "Invalid email or code");
        }
        User user = userOpt.get();

        // Validate the reset code and mark as used
        VerificationResult result = verificationService.validateAndThrowIfInvalid(
                user.getUuid(), code, CodeType.PASSWORD_RESET);

        if (result == VerificationResult.SUCCESS) {
            // Generate secure reset token
            PasswordResetToken resetToken = PasswordResetToken.builder()
                    .userUuid(user.getUuid())
                    .expiresAt(LocalDateTime.now().plusMinutes(15)) // 15-minute window for password reset
                    .verificationCodeUsed(passwordEncoder.encode(code))
                    .build();

            // Invalidate any existing tokens for this user
            passwordResetTokenRepository.invalidateAllTokensForUser(user.getUuid());
            
            // Save new token
            PasswordResetToken savedToken = passwordResetTokenRepository.save(resetToken);
            
            log.info("Password reset token generated for email: {}", email);
            return savedToken.getToken();
        }

        throw new BadRequestException("code", "Unable to verify reset code");
    }

    @Override
    public void resendPasswordResetCode(String email) {
        Optional<User> userOpt = userRepository.findByEmail(email);
        if (userOpt.isEmpty()) {
            // Don't reveal if email exists or not for security - but still log
            log.warn("Resend password reset code requested for non-existent email: {}", email);
            return;
        }
        
        User user = userOpt.get();

        // Check rate limiting
        if (!verificationService.canRequestNewCode(user.getUuid(), CodeType.PASSWORD_RESET)) {
            long remainingSeconds = verificationService.getRemainingCooldownSeconds(user.getUuid(), CodeType.PASSWORD_RESET);
            throw new BadRequestException("rate_limit", 
                String.format("Please wait %d seconds before requesting a new password reset code.", remainingSeconds));
        }

        // Invalidate existing reset tokens
        passwordResetTokenRepository.invalidateAllTokensForUser(user.getUuid());

        // Generate new password reset code
        String resetCode = codeGeneratorService.generateAlphanumericCode(6);

        // Resend verification code
        verificationService.resendVerificationCode(user.getUuid(),
                passwordEncoder.encode(resetCode),
                CodeType.PASSWORD_RESET);

        // Send password reset email
        emailService.sendEmail(user.getEmail(), resetCode, CodeType.PASSWORD_RESET);
        
        log.info("Password reset code resent to email: {}", email);
    }

    // Old resetPassword method removed - use resetPasswordWithToken instead

    @Override
    public String resetPasswordWithToken(String resetToken, String newPassword, String confirmPassword) {
        if (!newPassword.equals(confirmPassword)) {
            throw new BadRequestException("password", "Passwords do not match");
        }

        // Find and validate the reset token
        Optional<PasswordResetToken> tokenOpt = passwordResetTokenRepository.findByTokenAndUsedFalse(resetToken);
        if (tokenOpt.isEmpty()) {
            throw new BadRequestException("token", "Invalid or expired reset token");
        }

        PasswordResetToken token = tokenOpt.get();
        if (!token.isValid()) {
            throw new BadRequestException("token", "Reset token has expired");
        }

        User user = findUserByUuid(token.getUserUuid());

        // Validate password strength
        validatePasswordStrength(newPassword);

        // Update password
        user.setPassword(passwordEncoder.encode(newPassword));
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);

        // Mark token as used
        token.markAsUsed();
        passwordResetTokenRepository.save(token);

        // Invalidate all remaining password reset codes and tokens for security
        verificationService.invalidateExistingCodes(user.getUuid(), CodeType.PASSWORD_RESET);
        passwordResetTokenRepository.invalidateAllTokensForUser(user.getUuid());

        log.info("Password successfully reset using token for user: {}", user.getUuid());
        return "Password has been reset successfully";
    }

    // ========================================================================
    // USER INFORMATION - READ-ONLY ACCESS
    // ========================================================================

    @Override
    public UserDTO getUserById(String userId) {
        User user = findUserByUuid(userId);
        return userMapper.toDto(user);
    }

    @Override
    public UserDTO getUserByEmail(String email) {
        Optional<User> userOpt = userRepository.findByEmail(email);
        if (userOpt.isEmpty()) {
            throw new ResourceNotFoundException("User", "User with email ", email);
        }
        return userMapper.toDto(userOpt.get());
    }

    @Override
    public boolean isEmailVerified(String userId) {
        User user = findUserByUuid(userId);
        return user.getMailVerified();
    }

    @Override
    public boolean isAccountActive(String userId) {
        User user = findUserByUuid(userId);
        return user.getIsActive();
    }

    @Override
    public User.AccountType getAccountType(String userId) {
        User user = findUserByUuid(userId);
        return user.getAccountType();
    }

    // ========================================================================
    // ACCOUNT MANAGEMENT - AUTHENTICATION RESPONSIBILITY
    // ========================================================================

    @Override
    public void deactivateAccount(String userId) {
        User user = findUserByUuid(userId);
        user.setIsActive(false);
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);
    }

    @Override
    public void reactivateAccount(String userId) {
        User user = findUserByUuid(userId);
        user.setIsActive(true);
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);
    }

    @Override
    public void updateUserBasicInfo(String userId, UpdateUserBasicInfoRequest request) {
        User user = findUserByUuid(userId);

        if (request.getFirstName() != null) {
            user.setFirstName(request.getFirstName());
        }
        if (request.getLastName() != null) {
            user.setLastName(request.getLastName());
        }

        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);
    }

    @Override
    public void changePassword(String userId, String currentPassword, String newPassword, String confirmPassword) {
        if (!newPassword.equals(confirmPassword)) {
            throw new IllegalArgumentException("New passwords do not match");
        }

        User user = findUserByUuid(userId);

        // Verify current password
        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            throw new IllegalArgumentException("Current password is incorrect");
        }

        // Validate new password strength
        validatePasswordStrength(newPassword);

        // Update password
        user.setPassword(passwordEncoder.encode(newPassword));
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);
    }

    // ========================================================================
    // PROFILE STATUS ACCESS (READ-ONLY) - For Authentication Decisions
    // ========================================================================

    /**
     * Check if user has any profile - used for authentication decisions
     * This is a read-only check, profile creation/management is in ProfileService
     */
    @Override
    public boolean userHasProfile(String userId) {
        User user = findUserByUuid(userId);
        return user.hasPrimaryProfile();
    }

    /**
     * Check if user's profile is complete - used for role-based access decisions
     * This is a read-only check, profile management is in ProfileService
     */
    @Override
    public boolean userProfileIsComplete(String userId) {
        User user = findUserByUuid(userId);
        UserProfile primaryProfile = user.getPrimaryProfile();
        return primaryProfile != null && primaryProfile.isProfileComplete();
    }

    // ========================================================================
    // ROLE UPGRADE CALLBACK - Called by ProfileService
    // ========================================================================

    /**
     * This method is called by ProfileService when profile completion status changes
     * It's the only profile-related method that modifies user state in AuthService
     */
    @Override
    public void handleProfileCompletionChange(String userId) {
        User user = findUserByUuid(userId);

        if (!user.getMailVerified()) {
            return; // Email must be verified first
        }

        UserProfile primaryProfile = user.getPrimaryProfile();
        if (primaryProfile != null && primaryProfile.isProfileComplete()) {
            // Profile is complete, upgrade from PENDING role
            Role.RoleName newRole = determineRoleFromAccountType(user.getAccountType());
            if (newRole != null && !user.getAccountVerified()) {
                // Remove PENDING role and assign appropriate role
                rolePermissionService.removeRoleFromUser(user.getId(), Role.RoleName.PENDING);
                rolePermissionService.assignRoleToUser(user.getId(), newRole);

                // Mark account as fully verified
                user.setAccountVerified(true);
                user.setUpdatedAt(LocalDateTime.now());
                userRepository.save(user);
            }
        } else {
            // Profile is incomplete or deleted, downgrade to PENDING
            Role.RoleName currentRole = determineRoleFromAccountType(user.getAccountType());
            if (currentRole != null && user.getAccountVerified()) {
                rolePermissionService.removeRoleFromUser(user.getId(), currentRole);
                rolePermissionService.assignRoleToUser(user.getId(), Role.RoleName.PENDING);

                user.setAccountVerified(false);
                user.setUpdatedAt(LocalDateTime.now());
                userRepository.save(user);
            }
        }
    }

    // ========================================================================
    // HELPER METHODS
    // ========================================================================

    private User findUserByUuid(String userUuid) {
        return userRepository.findAll().stream()
                .filter(user -> userUuid.equals(user.getUuid()))
                .findFirst()
                .orElseThrow(() -> new UsernameNotFoundException("User not found with UUID: " + userUuid));
    }

    private void assignInitialRole(User user, User.AccountType accountType) {
        Role.RoleName initialRole;

        // All accounts start with PENDING role until profile is complete
        switch (accountType) {
            case ADMIN:
                // Admin gets immediate full access
                initialRole = Role.RoleName.ADMIN;
                user.setAccountVerified(true); // Admin accounts are immediately verified
                break;
            case DOCTOR:
            case PATIENT:
            case PHARMACY:
            case HOSPITAL:
            case INSURANCE:
            default:
                // All other accounts start with PENDING
                initialRole = Role.RoleName.PENDING;
                break;
        }

        rolePermissionService.assignRoleToUser(user.getId(), initialRole);
    }

    private Role.RoleName determineRoleFromAccountType(User.AccountType accountType) {
        return switch (accountType) {
            case DOCTOR -> Role.RoleName.DOCTOR;
            case PATIENT -> Role.RoleName.PATIENT;
            case ADMIN -> Role.RoleName.ADMIN;
            case PHARMACY -> Role.RoleName.PHARMACY_ADMIN;
            case HOSPITAL -> Role.RoleName.HOSPITAL_ADMIN;
            case INSURANCE -> Role.RoleName.INSURANCE_ADMIN;
            case LAB_TECHNICIAN -> Role.RoleName.LAB_TECHNICIAN;
            default -> null;
        };
    }

    private void sendNewVerificationCode(User user) {
        // Generate new verification code
        String newCode = codeGeneratorService.generateVerificationCode();

        // Resend verification code
        verificationService.resendVerificationCode(user.getUuid(),
                passwordEncoder.encode(newCode),
                CodeType.EMAIL_VERIFICATION);

        // Send verification email
        emailService.sendEmail(user.getEmail(), newCode, CodeType.EMAIL_VERIFICATION);
    }

    private void validatePasswordStrength(String password) {
        if (password == null || password.length() < 8) {
            throw new RuntimeException("Password must be at least 8 characters long");
        }

        boolean hasUpper = password.chars().anyMatch(Character::isUpperCase);
        boolean hasLower = password.chars().anyMatch(Character::isLowerCase);
        boolean hasDigit = password.chars().anyMatch(Character::isDigit);

        if (!hasUpper || !hasLower || !hasDigit) {
            throw new RuntimeException("Password must contain at least one uppercase letter, one lowercase letter, and one digit");
        }
    }

    // ========================================================================
    // SUPPORTING CLASSES
    // ========================================================================

    public static class UpdateUserBasicInfoRequest {
        private String firstName;
        private String lastName;

        // Getters and setters
        public String getFirstName() { return firstName; }
        public void setFirstName(String firstName) { this.firstName = firstName; }

        public String getLastName() { return lastName; }
        public void setLastName(String lastName) { this.lastName = lastName; }
    }
}