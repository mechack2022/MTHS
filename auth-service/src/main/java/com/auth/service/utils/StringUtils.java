package com.auth.service.utils;

import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class StringUtils {



    public static String generateVerificationToken() {
        return UUID.randomUUID().toString();
    }

    public static UUID generateNewUuid() {
        return UUID.randomUUID();
    }

    public static boolean isTokenValid(String token) {
        return token != null && !token.isEmpty();
    }

    public static String toUpperCaseSafe(String input) {
        return input == null ? null : input.trim().toUpperCase();
    }

    public static String generateVerificationCode() {
        int code = 10000 + (int)(Math.random() * 90000); // Ensures a 5-digit number
        return String.valueOf(code);
    }

}