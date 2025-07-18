package com.auth.service.entity;//package com.digi_dokita.entity;
//import com.digi_dokita.constants.Gender;
//import com.digi_dokita.constants.VerificationStatus;
//import jakarta.persistence.Entity;
//import jakarta.persistence.Table;
//import lombok.Builder;
//import lombok.Getter;
//import lombok.Setter;
//import jakarta.persistence.*;
//import lombok.*;
//
//import java.time.LocalDate;
//import java.time.LocalDateTime;
//
//@Entity
//@Table(name = "user_profiles")
//@Setter
//@Getter
//@Builder
//@NoArgsConstructor
//@AllArgsConstructor
//public class UserProfile {
//
//    @Id
//    @GeneratedValue(strategy = GenerationType.IDENTITY) // or AUTO
//    private Long id;
//
//    @OneToOne
//    @JoinColumn(name = "user_id", nullable = false, unique = true)
//    private User user;
//
//    @Column(name = "date_of_birth")
//    private LocalDate dateOfBirth;
//
//    @Enumerated(EnumType.STRING)
//    private Gender gender;
//
//    private String bio;
//
//    @Column(name = "professional_license_number")
//    private String professionalLicenseNumber;
//
//    @Column(name = "phone_number")
//    private String phoneNumber;
//
//    private String specialization;
//
//    @Column(columnDefinition = "TEXT")
//    private String boardCertifications;
//
//    @Enumerated(EnumType.STRING)
//    private VerificationStatus verificationStatus;
//
//    @Column(name = "verified_by")
//    private String verifiedBy;
//
//    @Column(name = "profile_picture_url")
//    private String profilePictureUrl;
//
//    @Column(name = "created_at", updatable = false)
//    private LocalDateTime createdAt;
//
//    @Column(name = "updated_at")
//    private LocalDateTime updatedAt;
//}
