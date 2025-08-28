package com.auth.service.service;

import com.auth.service.entity.User;
import com.auth.service.exceptions.BadRequestException;
import com.auth.service.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProfileCreationTokenService {

    private final UserRepository userRepository;
    
    // In-memory store for profile creation tokens (expires in 24 hours)
    private final ConcurrentHashMap<String, ProfileCreationTokenInfo> tokens = new ConcurrentHashMap<>();
    
    private static class ProfileCreationTokenInfo {
        final String userId;
        final String userEmail;
        final LocalDateTime expiresAt;
        boolean used;
        
        ProfileCreationTokenInfo(String userId, String userEmail, LocalDateTime expiresAt) {
            this.userId = userId;
            this.userEmail = userEmail;
            this.expiresAt = expiresAt;
            this.used = false;
        }
    }
    
    /**
     * Generate a profile creation token for a fully verified user
     */
    public String generateProfileCreationToken(String userId) {
        User user = userRepository.findByUuid(userId)
                .orElseThrow(() -> new BadRequestException("user", "User not found"));
        
        // Only verify email is verified (account verification happens AFTER profile creation)
        if (!user.getMailVerified()) {
            throw new BadRequestException("email", "Email must be verified before creating profile");
        }
        
        // Check if user is active
        if (!user.getIsActive()) {
            throw new BadRequestException("account", "Account is inactive");
        }
        
        // Generate secure token
        String token = generateSecureToken();
        
        // Store token info (expires in 24 hours)
        ProfileCreationTokenInfo tokenInfo = new ProfileCreationTokenInfo(
            userId,
            user.getEmail(),
            LocalDateTime.now().plusHours(24)
        );
        
        tokens.put(token, tokenInfo);
        
        log.info("Generated profile creation token for fully verified user: {} ({})", userId, user.getEmail());
        return token;
    }
    
    /**
     * Validate profile creation token and return user info
     */
    public ValidatedTokenInfo validateProfileCreationToken(String token, String targetUserId) {
        if (token == null || token.trim().isEmpty()) {
            throw new BadRequestException("token", "Profile creation token is required");
        }
        
        ProfileCreationTokenInfo tokenInfo = tokens.get(token);
        if (tokenInfo == null) {
            throw new BadRequestException("token", "Invalid or expired profile creation token");
        }
        
        // Check if token is expired
        if (LocalDateTime.now().isAfter(tokenInfo.expiresAt)) {
            tokens.remove(token);
            throw new BadRequestException("token", "Profile creation token has expired");
        }
        
        // Check if token has been used
        if (tokenInfo.used) {
            throw new BadRequestException("token", "Profile creation token has already been used");
        }
        
        // Verify token is for the correct user
        if (!tokenInfo.userId.equals(targetUserId)) {
            throw new BadRequestException("token", "Token is not valid for this user");
        }
        
        // Double-check user email is still verified and active
        User user = userRepository.findByUuid(targetUserId)
                .orElseThrow(() -> new BadRequestException("user", "User not found"));
        
        if (!user.getMailVerified() || !user.getIsActive()) {
            throw new BadRequestException("verification", "User verification status has changed. Cannot create profile.");
        }
        
        // Mark token as used
        tokenInfo.used = true;
        
        log.info("Validated profile creation token for fully verified user: {} ({})", tokenInfo.userId, tokenInfo.userEmail);
        
        return new ValidatedTokenInfo(tokenInfo.userId, tokenInfo.userEmail);
    }
    
    /**
     * Check if user can create profile (email verified and active)
     */
    public boolean canUserCreateProfile(String userId) {
        return userRepository.findByUuid(userId)
                .map(user -> user.getMailVerified() && user.getIsActive())
                .orElse(false);
    }
    
    /**
     * Get user verification status for debugging
     */
    public UserVerificationStatus getUserVerificationStatus(String userId) {
        User user = userRepository.findByUuid(userId)
                .orElseThrow(() -> new BadRequestException("user", "User not found"));
        
        return new UserVerificationStatus(
            user.getMailVerified(),
            user.getAccountVerified(),
            user.getIsActive(),
            user.getVerificationStatus()
        );
    }
    
    /**
     * Clean up expired tokens (can be called periodically)
     */
    public void cleanupExpiredTokens() {
        LocalDateTime now = LocalDateTime.now();
        tokens.entrySet().removeIf(entry -> now.isAfter(entry.getValue().expiresAt));
        log.debug("Cleaned up expired profile creation tokens");
    }
    
    private String generateSecureToken() {
        SecureRandom secureRandom = new SecureRandom();
        byte[] tokenBytes = new byte[32];
        secureRandom.nextBytes(tokenBytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(tokenBytes);
    }
    
    public static class ValidatedTokenInfo {
        public final String userId;
        public final String userEmail;
        
        public ValidatedTokenInfo(String userId, String userEmail) {
            this.userId = userId;
            this.userEmail = userEmail;
        }
    }
    
    public static class UserVerificationStatus {
        public final boolean emailVerified;
        public final boolean accountVerified;
        public final boolean isActive;
        public final com.auth.service.constants.VerificationStatus verificationStatus;
        
        public UserVerificationStatus(boolean emailVerified, boolean accountVerified, 
                                    boolean isActive, com.auth.service.constants.VerificationStatus verificationStatus) {
            this.emailVerified = emailVerified;
            this.accountVerified = accountVerified;
            this.isActive = isActive;
            this.verificationStatus = verificationStatus;
        }
    }
}