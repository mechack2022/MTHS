package com.auth.service.service;

import com.auth.service.constants.CodeType;

public interface EmailService {
//    void sendVerificationEmail(String recipientEmail, String code, CodeType codeType);

    void sendVerificationCode(String email, String code);

    void sendPasswordResetCode(String email, String resetCode, CodeType codeType);
}
