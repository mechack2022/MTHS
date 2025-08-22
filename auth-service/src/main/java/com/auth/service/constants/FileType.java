package com.auth.service.constants;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public enum FileType {

    PROFILE_IMAGE(
            "profile-images",
            List.of("image/jpeg", "image/png", "image/webp", "image/jpg"),
            5 * 1024 * 1024 // 5MB
    ),

    PATIENT_DOCUMENT(
            "patient-documents",
            List.of("application/pdf", "image/jpeg", "image/png"),
            10 * 1024 * 1024
    ),

    PRESCRIPTION(
            "prescriptions",
            List.of("application/pdf"),
            10 * 1024 * 1024
    ),

    MEDICAL_REPORT(
            "medical-reports",
            List.of("application/pdf", "image/jpeg", "image/png"),
            15 * 1024 * 1024
    );

    private final String folder;
    private final List<String> allowedMimeTypes;
    private final long maxSize;
}

