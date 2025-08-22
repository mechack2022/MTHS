//package com.auth.service.entity.doctor;
//
//import com.auth.service.constants.Gender;
//import com.auth.service.entity.BaseEntity;
//import com.auth.service.entity.User;
//import jakarta.persistence.*;
//import lombok.*;
//import org.hibernate.proxy.HibernateProxy;
//
//import java.time.LocalDate;
//import java.util.HashSet;
//import java.util.Objects;
//import java.util.Set;
//
//@Setter
//@Getter
//@ToString
//@Entity
//@Table(name = "doctors")
//@DiscriminatorValue("DOCTOR")
//@NoArgsConstructor
//@AllArgsConstructor
//public class Doctor extends User {
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
//    @Column(name = "phone_number", unique = true)
//    private String phoneNumber;
//
//    private String specialization;
//
//    @Column(columnDefinition = "TEXT")
//    private String experience;
//
//    @Column(name = "license_id", unique = true)
//    private String licenseId;
//
//    @Column(name = "practice_address")
//    private String practiceAddress;
//
//    @Column(columnDefinition = "TEXT")
//    private String boardCertifications;
//
//    @Column(name = "profile_picture_url")
//    private String profilePictureUrl;
//    // File URLs
//    @Column(name = "profile_image_url")
//    private String profileImageUrl;
//
//    @Column(name ="certificate_url", unique = true)
//    private String certificateUrl;
//
//    // One doctor can have many patients
////    @OneToMany(mappedBy = "assignedDoctor", fetch = FetchType.LAZY)
////    private Set<Patient> patients = new HashSet<>();
//
//    @Override
//    public final boolean equals(Object o) {
//        if (this == o) return true;
//        if (o == null) return false;
//        Class<?> oEffectiveClass = o instanceof HibernateProxy ? ((HibernateProxy) o).getHibernateLazyInitializer().getPersistentClass() : o.getClass();
//        Class<?> thisEffectiveClass = this instanceof HibernateProxy ? ((HibernateProxy) this).getHibernateLazyInitializer().getPersistentClass() : this.getClass();
//        if (thisEffectiveClass != oEffectiveClass) return false;
//        Doctor doctor = (Doctor) o;
//        return getId() != null && Objects.equals(getId(), doctor.getId());
//    }
//
//    @Override
//    public final int hashCode() {
//        return this instanceof HibernateProxy ? ((HibernateProxy) this).getHibernateLazyInitializer().getPersistentClass().hashCode() : getClass().hashCode();
//    }
//
//    public DoctorDTO toDto(Doctor req){
//        return null;
//    }
//
//    public Doctor toEntity(Doctor req){
//        return null;
//    }
//}
