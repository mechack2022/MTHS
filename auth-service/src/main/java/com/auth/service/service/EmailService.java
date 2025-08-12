package com.auth.service.service;

import com.auth.service.constants.CodeType;

public interface EmailService {
//    void sendVerificationEmail(String recipientEmail, String code, CodeType codeType);

//  codeType  void sendVerificationCode(String email, String code);
//
//    void sendPasswordResetCode(String email, String resetCode, CodeType codeType);
     public void sendEmail(String email, String resetCode, CodeType codeType);
//    /**
//     * Send verification code to user's email
//     * @param email User's email address
//     * @param code Verification code
//     */
//    void sendVerificationCode(String email, String code);
//
//    /**
//     * Send password reset code to user's email
//     * @param email User's email address
//     * @param resetCode Password reset code
//     * @param codeType Type of the code being sent
//     */
//    void sendPasswordResetCode(String email, String resetCode, CodeType codeType);
//
//    /**
//     * Send welcome email to newly registered user
//     * @param email User's email address
//     * @param firstName User's first name
//     */
//    void sendWelcomeEmail(String email, String firstName);
//
//    /**
//     * Send password reset confirmation email
//     * @param email User's email address
//     * @param firstName User's first name
//     */
//    void sendPasswordResetConfirmation(String email, String firstName);
//
//    void sendPasswordResetEmail(String email, String resetCode);
}
