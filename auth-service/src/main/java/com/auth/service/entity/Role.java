package com.auth.service.entity;//package com.digi_dokita.entity;
//import com.digi_dokita.constants.RoleName;
//import jakarta.persistence.Entity;
//import jakarta.persistence.Table;
//import lombok.Builder;
//import lombok.Getter;
//import lombok.Setter;
//import jakarta.persistence.*;
//import lombok.*;
//
//import java.time.LocalDateTime;
//
//@Entity
//@Table(name = "roles")
//@Setter
//@Getter
//@Builder
//@NoArgsConstructor
//@AllArgsConstructor
//public class Role {
//
//    @Id
//    @GeneratedValue(strategy = GenerationType.IDENTITY) // or AUTO
//    private Long id;
//
//    @Enumerated(EnumType.STRING)
//    @Column(unique = true, nullable = false)
//    private RoleName roleName;
//
//    private String description;
//
//    @Column(name = "created_at")
//    private LocalDateTime createdAt;
//}
