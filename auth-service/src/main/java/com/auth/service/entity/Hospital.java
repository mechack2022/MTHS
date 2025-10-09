//package com.auth.service.entity;
//
//import jakarta.persistence.*;
//import lombok.Data;
//import lombok.EqualsAndHashCode;
//
//import java.time.LocalDateTime;
//
//@Entity
//@Table(name = "hospitals")
//@Data
//@EqualsAndHashCode(callSuper = false)
//public class Hospital {
//    @Id
//    @GeneratedValue(strategy = GenerationType.IDENTITY)
//    @Column(name = "hospital_id")
//    private Long hospitalId;
//
//    @Column(name = "name", nullable = false)
//    private String name;
//
//    @Column(name = "care_type")
//    private String careType;
//
//    @Column(name = "address")
//    private String address;
//
//    @Column(name = "city")
//    private String city;
//
//    @Column(name = "state")
//    private String state;
//
//    @Column(name = "zipcode")
//    private String zipcode;
//
//    @Column(name = "county")
//    private String county;
//
//    @Column(name = "location_area_code")
//    private String locationAreaCode;
//
//    @Column(name = "fips_code")
//    private String fipsCode;
//
//    @Column(name = "timezone")
//    private String timezone;
//
//    @Column(name = "latitude")
//    private String latitude;
//
//    @Column(name = "longitude")
//    private String longitude;
//
//    @Column(name = "phone_number")
//    private String phoneNumber;
//
//    @Column(name = "website")
//    private String website;
//
//    @Column(name = "ownership")
//    private String ownership;
//
//    @Column(name = "bedcount")
//    private Integer bedcount;
//
//    @Column(name = "rating")
//    private Double rating;
//
//    @Column(name = "services_offered", columnDefinition = "TEXT")
//    private String servicesOffered;
//
//    @Column(name = "created_at")
//    private LocalDateTime createdAt;
//
//    @Column(name = "updated_at")
//    private LocalDateTime updatedAt;
//
//    @PrePersist
//    protected void onCreate() {
//        createdAt = LocalDateTime.now();
//        updatedAt = LocalDateTime.now();
//    }
//
//    @PreUpdate
//    protected void onUpdate() {
//        updatedAt = LocalDateTime.now();
//    }
//}