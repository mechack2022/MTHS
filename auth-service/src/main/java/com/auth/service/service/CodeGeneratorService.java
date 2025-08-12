package com.auth.service.service;

import java.security.SecureRandom;

import org.springframework.stereotype.Service;

@Service
public class CodeGeneratorService {

    private static final String NUMERIC_CHARS = "0123456789";
    private static final int DEFAULT_CODE_LENGTH = 6;
    private final SecureRandom random = new SecureRandom();

    /**
     * Generate a random numeric verification code
     * @return 6-digit verification code
     */
    public String generateVerificationCode() {
        return generateVerificationCode(DEFAULT_CODE_LENGTH);
    }

    /**
     * Generate a random numeric verification code with specified length
     * @param length Length of the code
     * @return Numeric verification code
     */
    public String generateVerificationCode(int length) {
        StringBuilder code = new StringBuilder(length);

        for (int i = 0; i < length; i++) {
            int index = random.nextInt(NUMERIC_CHARS.length());
            code.append(NUMERIC_CHARS.charAt(index));
        }

        return code.toString();
    }

    /**
     * Generate a random alphanumeric code
     * @param length Length of the code
     * @return Alphanumeric code
     */
    public String generateAlphanumericCode(int length) {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        StringBuilder code = new StringBuilder(length);

        for (int i = 0; i < length; i++) {
            int index = random.nextInt(chars.length());
            code.append(chars.charAt(index));
        }

        return code.toString();
    }
}