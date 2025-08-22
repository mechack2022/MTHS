//package com.auth.service.entity.doctor;
//
//import com.auth.service.constants.Gender;
//import jakarta.mail.Multipart;
//import jakarta.validation.constraints.*;
//import org.hibernate.validator.constraints.URL;
//
//public class DoctorDTO {
//
//    @Null(groups = DoctorValidationGroups.OnCreate.class, message = "ID will be auto-generated")
//    @NotNull(groups = DoctorValidationGroups.OnUpdate.class, message = "ID is required for updates")
//    private Long id;
//
//    @NotNull(message = "Gender is required")
//    private Gender gender;
//
//    @Size(max = 1000, message = "Bio cannot exceed 1000 characters")
//    private String bio;
//
//    @NotBlank(message = "Professional license number is required")
//    @Size(min = 5, max = 50, message = "Professional license number must be between 5 and 50 characters")
//    private String professionalLicenseNumber;
//
//    @NotBlank(message = "Phone number is required")
//    @Pattern(regexp = "^\\+?[0-9. ()-]{7,}$", message = "Phone number must be valid (e.g., +1-555-123-4567)")
//    private String phoneNumber;
//
//    @NotBlank(message = "Specialization is required")
//    @Size(max = 100, message = "Specialization cannot exceed 100 characters")
//    private String specialization;
//
//    @Size(max = 500, message = "Experience description cannot exceed 500 characters")
//    private String experience;
//
//    @NotBlank(message = "License ID is required")
//    @Size(max = 100, message = "License ID cannot exceed 100 characters")
//    private String licenseId;
//
//    @NotBlank(message = "Practice address is required")
//    @Size(max = 255, message = "Practice address cannot exceed 255 characters")
//    private String practiceAddress;
//
//    @Size(max = 1000, message = "Board certifications cannot exceed 1000 characters")
//    private String boardCertifications;
//
//    // Optional: if URL format should be validated
//    @URL(message = "Profile image URL must be a valid URL", regexp = "^(https?://.*)?$", flags = Pattern.Flag.CASE_INSENSITIVE, payload = {})
//    @Size(max = 500, message = "Profile picture URL cannot exceed 500 characters")
//    private String profileImageUrl;
//
//    // Updated field in DoctorDTO
//    @NotBlank(message = "Certificate upload is required for verification", groups = DoctorValidationGroups.OnCreate.class)
//    @URL(message = "Certificate URL must be a valid URL", payload = {})
//    private String certificateUrl;
//
//}
