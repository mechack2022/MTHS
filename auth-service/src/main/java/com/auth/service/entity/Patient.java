
//package com.auth.service.entity;
//
//import com.auth.service.constants.Gender;
//import com.auth.service.entity.BaseEntity;
//import com.auth.service.entity.User;
//import com.fasterxml.jackson.annotation.JsonIgnore;
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
//@Table(name = "patients")
//@DiscriminatorValue("PATIENT")
//@NoArgsConstructor
//@AllArgsConstructor
//public class Patient extends User {
//
//    @OneToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "user_id", nullable = false, unique = true)
//    @JsonIgnore
//    private User user;
//
//    @Column(name = "date_of_birth")
//    private LocalDate dateOfBirth;
//
//    @Enumerated(EnumType.STRING)
//    private Gender gender;
//
//    @Column(name = "phone_number", unique = true)
//    private String phoneNumber;
//
//    @Column(name = "nin", unique = true)
//    private String NIN;
//
//    @Column(name = "address")
//    private String address;
//
//    @Column(name = "marital_status")
//    private String marital_status;
//
//    // File URLs
//    @Column(name = "profile_image_url")
//    private String profileImageUrl;
//
//    @Column(name ="preferred_hospital")
//    private String preferredHospital;
//
//    @Column(name ="preferred_lab")
//    private String preferredLaboratory;
//
//    @Column(name ="preferred_pharmacy")
//    private String preferredPharmacy;
//
//    // One doctor can have many patients
////    @OneToMany(mappedBy = "assignedDoctor", fetch = FetchType.LAZY)
////    private Set<Patient> patients = new HashSet<>();
//
//    public boolean isProfileComplete() {
//        return dateOfBirth != null &&
//                gender != null &&
//                phoneNumber != null && !phoneNumber.trim().isEmpty() &&
//                address != null && !address.trim().isEmpty() &&
//                NIN != null && !NIN.trim().isEmpty();
//    }
//
//    @Override
//    public final boolean equals(Object o) {
//        if (this == o) return true;
//        if (o == null) return false;
//        Class<?> oEffectiveClass = o instanceof HibernateProxy ? ((HibernateProxy) o).getHibernateLazyInitializer().getPersistentClass() : o.getClass();
//        Class<?> thisEffectiveClass = this instanceof HibernateProxy ? ((HibernateProxy) this).getHibernateLazyInitializer().getPersistentClass() : this.getClass();
//        if (thisEffectiveClass != oEffectiveClass) return false;
//        Patient doctor = (Patient) o;
//        return getId() != null && Objects.equals(getId(), doctor.getId());
//    }
//
//    @Override
//    public final int hashCode() {
//        return this instanceof HibernateProxy ? ((HibernateProxy) this).getHibernateLazyInitializer().getPersistentClass().hashCode() : getClass().hashCode();
//    }
//}
