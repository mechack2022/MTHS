package com.auth.service.service;

import com.auth.service.constants.VerificationResult;
import com.auth.service.dto.TokenResponse;
import com.auth.service.dto.UserDTO;
import com.auth.service.entity.User;
import org.springframework.data.domain.Page;

public interface UserAuthService {

    // ========================================================================
    // AUTHENTICATION METHODS
    // ========================================================================

    UserDTO createUser(UserDTO req);
    TokenResponse login(String username, String password);
    void logout(String refreshToken);

    // ========================================================================
    // EMAIL VERIFICATION METHODS
    // ========================================================================

    /**
     * Verify user email with verification code
     * @param userId User UUID
     * @param code Verification code
     * @return VerificationResult indicating success or failure reason
     */
    VerificationResult verifyEmailWithCode(String userId, String code);

    /**
     * Resend email verification code
     * @param email User email address
     * @return Success message
     * @throws BadRequestException if rate limited or user not found
     */
    String resendEmailVerificationCode(String email);

    /**
     * Mark user email as verified (internal use)
     * @param userId User UUID
     */
    void markEmailAsVerified(String userId);

    // ========================================================================
    // PASSWORD RESET METHODS
    // ========================================================================

    /**
     * Initiate password reset process
     * @param email User email address
     * @throws BadRequestException if rate limited
     */
    void initiatePasswordReset(String email);

    /**
     * Verify password reset code and generate reset token
     * @param email User email address
     * @param code Reset verification code
     * @return Reset token for password change
     * @throws BadRequestException if code is invalid, expired, or not found
     */
    String verifyResetPasswordCode(String email, String code);

    /**
     * Resend password reset code
     * @param email User email address
     * @throws BadRequestException if rate limited
     */
    void resendPasswordResetCode(String email);

    /**
     * Reset password using reset token
     * @param resetToken Valid reset token from verification step
     * @param newPassword New password
     * @param confirmPassword Password confirmation
     * @return Success message
     * @throws BadRequestException if token is invalid or passwords don't match
     */
    String resetPasswordWithToken(String resetToken, String newPassword, String confirmPassword);

    /**
     * Change password for authenticated user
     * @param userId User UUID
     * @param currentPassword Current password
     * @param newPassword New password
     * @param confirmPassword Password confirmation
     */
    void changePassword(String userId, String currentPassword, String newPassword, String confirmPassword);

    // ========================================================================
    // USER INFORMATION METHODS (READ-ONLY)
    // ========================================================================

    UserDTO getUserById(String userId);
    UserDTO getUserByEmail(String email);
    boolean isEmailVerified(String userId);
    boolean isAccountActive(String userId);
    User.AccountType getAccountType(String userId);

    // ========================================================================
    // ACCOUNT MANAGEMENT METHODS
    // ========================================================================

    void deactivateAccount(String userId);
    void reactivateAccount(String userId);
    void updateUserBasicInfo(String userId, UserAuthServiceImpl.UpdateUserBasicInfoRequest request);

    // ========================================================================
    // PROFILE STATUS ACCESS (READ-ONLY)
    // ========================================================================

    boolean userHasProfile(String userId);
    boolean userProfileIsComplete(String userId);

    // ========================================================================
    // CALLBACK METHOD (Called by ProfileService)
    // ========================================================================

    void handleProfileCompletionChange(String userId);

    // ========================================================================
    // ADMIN VERIFICATION METHODS
    // ========================================================================

    /**
     * Get pending users for admin verification
     * @param pageable Pagination parameters
     * @return Page of pending users
     */
    Page<UserDTO> getPendingUsers(org.springframework.data.domain.Pageable pageable);

    /**
     * Verify user account by admin
     * @param userId User UUID
     * @return Updated user DTO
     */
    UserDTO verifyUserAccount(String userId);

    /**
     * Reject user account by admin
     * @param userId User UUID
     * @param reason Rejection reason
     */
    void rejectUserAccount(String userId, String reason);
}
