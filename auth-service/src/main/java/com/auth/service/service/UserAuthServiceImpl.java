package com.auth.service.service;

import com.auth.service.dto.TokenResponse;
import com.auth.service.dto.UserDTO;
import com.auth.service.entity.User;
import com.auth.service.entity.Role;
import com.auth.service.exceptions.BadRequestException;
import com.auth.service.exceptions.ResourceNotFoundException;
import com.auth.service.mapper.UserMapper;
import com.auth.service.repository.UserRepository;
import com.auth.service.repository.PasswordResetRepository;
import com.auth.service.repository.RoleRepository;
import com.auth.service.constants.CodeType;
import com.auth.service.constants.VerificationResult;
import com.auth.service.jwt.JwtTokenProvider;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional
public class UserAuthServiceImpl implements UserAuthService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordResetRepository passwordResetRepository;

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

    @Override
    public UserDTO createUser(UserDTO req) {
        // Check if user already exists
        if (userRepository.existsByEmail(req.getEmail())) {
            throw new BadRequestException("email", req.getEmail() + " has been taken");
        }

        // Create new user
        User user = new User();
        user.setUuid(UUID.randomUUID().toString());
        user.setEmail(req.getEmail());
        user.setPassword(passwordEncoder.encode(req.getPassword()));
        user.setFirstName(req.getFirstName());
        user.setLastName(req.getLastName());
        user.setMailVerified(false);
        user.setAccountVerified(false);
        user.setIsActive(true);
        user.setAccountType(req.getAccountType());
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());

        // Save user first
        User savedUser = userRepository.save(user);
        assignDefaultRole(user, req.getAccountType());

        // Generate and save verification code using your existing service
        String verificationCode = codeGeneratorService.generateVerificationCode(5);
        verificationService.saveVerificationCode(savedUser.getUuid(),
                passwordEncoder.encode(verificationCode),
                CodeType.EMAIL_VERIFICATION);

        // Send verification email with plain code
        emailService.sendEmail(savedUser.getEmail(), verificationCode, CodeType.EMAIL_VERIFICATION);

        return userMapper.toDto(savedUser);
    }


    @Override
    public TokenResponse login(String username, String password) {
        // Find user by email (username is treated as email)
        Optional<User> userOpt = userRepository.findByEmail(username);

        if (userOpt.isEmpty()) {
            throw new BadRequestException("username or password", "Invalid email or password");
        }

        User user = userOpt.get();

        // Check if user is active
        if (!user.getAccountVerified()) {
            throw new BadRequestException("accountType", "Account is inactive. Please contact support.");
        }

        // Verify password
        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new IllegalArgumentException("Invalid email or password");
        }

        // Check if email is verified
        if (!user.getMailVerified()) {
            throw new RuntimeException("Email not verified. Please verify your email first.");
        }

        // Generate new access token
        String newAccessToken = jwtTokenProvider.generateToken(username);
        String newRefreshToken = jwtTokenProvider.generateRefreshToken(username);
        // Update user timestamps
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);

        return new TokenResponse(
                newAccessToken,
                newRefreshToken, // You can return the same refresh token or a new one
                (int) jwtTokenProvider.getTokenExpiration()
        );
    }

    @Override
    public void logout(String refreshToken) {
        try {
            // Validate the refresh token
            if (jwtTokenProvider.validateToken(refreshToken) && jwtTokenProvider.isRefreshToken(refreshToken)) {
                String username = jwtTokenProvider.getUsername(refreshToken);
                Optional<User> userOpt = userRepository.findByEmail(username);

                if (userOpt.isPresent()) {
                    User user = userOpt.get();
                    user.setUpdatedAt(LocalDateTime.now());
                    userRepository.save(user);
                    // Log the logout event if needed
                    // logService.logLogout(user.getId());
                }
            }
        } catch (Exception e) {
            System.err.println("Error during logout: " + e.getMessage());
        }
    }

    @Override
    public VerificationResult verifyEmailWithCode(String userId, String code) {
        User user = findUserByUuid(userId);

        if (user.getMailVerified()) {
            throw new BadRequestException(user.getEmail(), "Email is already verified");
        }
        // Use your existing verification service to validate the code
        VerificationResult verificationResult = verificationService.validateAndThrowIfInvalid(userId, code, CodeType.EMAIL_VERIFICATION);
        // Mark email as verified
        user.setMailVerified(true);
//        user.setAccountVerified(true);
        user.setUpdatedAt(LocalDateTime.now());
        // assigns user role and permission for this mail verification
        userRepository.save(user);
        return verificationResult;
    }

    @Override
    public void resendVerificationCode(String userId, String email) {
        User user = findUserByUuid(userId);

        if (Boolean.TRUE.equals(user.getMailVerified())) {
            throw new BadRequestException("email", "Unverified Email");
        }
        // Verify email matches user
        if (!user.getEmail().equals(email)) {
            throw new BadRequestException("email","Email does not match user account");
        }
        sendNewVerificationCode(user);
    }

    @Override
    public String resendVerificationCode(String email) {
        Optional<User> userOpt = userRepository.findByEmail(email);
        if (userOpt.isEmpty()) {
            throw new ResourceNotFoundException("User", "User with email ", email);
        }
        User user = userOpt.get();
        if (Boolean.TRUE.equals(user.getMailVerified())) {
            throw new BadRequestException("email", "Email is already verified");
        }

        sendNewVerificationCode(user);
        return "Email verification resend token resend";
    }

    @Override
    public void initiatePasswordReset(String email) {
        Optional<User> userOpt = userRepository.findByEmail(email);
        if (userOpt.isEmpty()) {
            // Don't reveal if email exists or not for security
            return;
        }
        User user = userOpt.get();
        // Generate password reset code
        String resetCode = codeGeneratorService.generateAlphanumericCode(5);
        // Save verification code using your existing service
        verificationService.saveVerificationCode(user.getUuid(),
                passwordEncoder.encode(resetCode),
                CodeType.PASSWORD_RESET);
        // Send password reset email with plain reset code
        emailService.sendEmail(user.getEmail(), resetCode, CodeType.PASSWORD_RESET);
    }

    @Override
    public void verifyResetPasswordCode(String email, String code) {
        Optional<User> userOpt = userRepository.findByEmail(email);
        if (userOpt.isEmpty()) {
            throw new ResourceNotFoundException("user","User with email ", email );
        }
        User user = userOpt.get();
        // Use your existing verification service to validate the reset code
        VerificationResult result = verificationService.validateVerificationCode(user.getUuid(), code, CodeType.PASSWORD_RESET);
        switch (result) {
            case SUCCESS:
                return;
            case CODE_NOT_FOUND:
                throw new BadRequestException("code", "No active password reset request found for this code : "+ code );
            case CODE_EXPIRED:
                throw new BadRequestException("code", "Reset code has expired");
            case INVALID_CODE:
                throw new BadRequestException("code", "Invalid reset code");
            case CODE_ALREADY_USED:
                throw new BadRequestException("code","Reset code has already been used");
            default:
                throw new BadRequestException("code", "Invalid reset code");
        }
    }

    @Override
    public void resendPasswordResetCode(String email) {
        Optional<User> userOpt = userRepository.findByEmail(email);
        if (userOpt.isEmpty()) {
            // Don't reveal if email exists or not for security
            return;
        }
        User user = userOpt.get();
        // Generate new password reset code
        String resetCode = codeGeneratorService.generateAlphanumericCode(5);
        // Use your existing verification service to resend code
        verificationService.resendVerificationCode(user.getUuid(),
                passwordEncoder.encode(resetCode),
                CodeType.PASSWORD_RESET);
        // Send password reset email
        emailService.sendEmail(user.getEmail(), resetCode, CodeType.PASSWORD_RESET);
    }

    @Override
    public String resetPassword(String userId, String newPassword, String confirmPassword) {
        if (!newPassword.equals(confirmPassword)) {
            throw new IllegalArgumentException("Passwords do not match");
        }

        User user = findUserByUuid(userId);

        // Check if user has a valid password reset code
        if (!verificationService.hasValidCode(userId, CodeType.PASSWORD_RESET)) {
            throw new RuntimeException("No active password reset request found or request has expired");
        }
        // Validate password strength
        validatePasswordStrength(newPassword);
        // Update password
        user.setPassword(passwordEncoder.encode(newPassword));
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);
        // Invalidate all password reset codes for this user
        verificationService.invalidateExistingCodes(userId, CodeType.PASSWORD_RESET);
        return "Password Reset Successfully";
    }

    @Override
    public void markEmailAsVerified(String userId) {
        User user = findUserByUuid(userId);

        if (user.getMailVerified()) {
            return; // Already verified
        }

        user.setMailVerified(true);
        user.setAccountVerified(true);
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);

        // Invalidate all email verification codes for this user
        verificationService.invalidateExistingCodes(userId, CodeType.EMAIL_VERIFICATION);
    }

    // Helper methods
    private User findUserByUuid(String userUuid) {
        // You should add findByUuid method to your UserRepository for better performance
        return userRepository.findAll().stream()
                .filter(user -> userUuid.equals(user.getUuid()))
                .findFirst()
                .orElseThrow(() -> new UsernameNotFoundException("User not found with UUID: " + userUuid));
    }

    private void assignDefaultRole(User user, User.AccountType accountType) {
        Role.RoleName defaultRoleName;
        // Assign role based on account type
        switch (user.getAccountType()) {
            case DOCTOR:
                defaultRoleName = Role.RoleName.DOCTOR;
                break;
            case ADMIN:
                defaultRoleName = Role.RoleName.ADMIN;
                break;
            case PHARMACY:
                defaultRoleName = Role.RoleName.PHARMACY_ADMIN;
                break;
            case HOSPITAL:
                defaultRoleName = Role.RoleName.HOSPITAL_ADMIN;
                break;
            case INSURANCE:
                defaultRoleName = Role.RoleName.INSURANCE_ADMIN;
                break;
            case PATIENT:
            default:
                defaultRoleName = Role.RoleName.PATIENT;
                break;
        }

        rolePermissionService.assignRoleToUser(user.getId(), defaultRoleName);
    }

    private void sendNewVerificationCode(User user) {
        // Generate new verification code
        String newCode = codeGeneratorService.generateVerificationCode();

        // Use your existing verification service to resend code
        verificationService.resendVerificationCode(user.getUuid(),
                passwordEncoder.encode(newCode),
                CodeType.EMAIL_VERIFICATION);

        // Send verification email with plain code
        emailService.sendEmail(user.getEmail(), newCode, CodeType.EMAIL_VERIFICATION);
    }

    private void validatePasswordStrength(String password) {
        if (password == null || password.length() < 8) {
            throw new RuntimeException("Password must be at least 8 characters long");
        }
        // Add more validation rules as needed
        boolean hasUpper = password.chars().anyMatch(Character::isUpperCase);
        boolean hasLower = password.chars().anyMatch(Character::isLowerCase);
        boolean hasDigit = password.chars().anyMatch(Character::isDigit);
        boolean hasSpecial = password.chars().anyMatch(ch -> "!@#$%^&*()_+-=[]{}|;:,.<>?".indexOf(ch) >= 0);

        if (!hasUpper || !hasLower || !hasDigit) {
            throw new RuntimeException("Password must contain at least one uppercase letter, one lowercase letter, and one digit");
        }
    }
}