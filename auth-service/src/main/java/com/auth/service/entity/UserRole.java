package com.auth.service.entity;//package com.digi_dokita.entity;
//import com.digi_dokita.dto.UserRoleId;
//import jakarta.persistence.Entity;
//import jakarta.persistence.Table;
//import lombok.Builder;
//import jakarta.persistence.*;
//import lombok.*;
//
//import java.time.LocalDateTime;
//
//@Entity
//@Table(name = "user_roles")
//@Getter
//@Setter
//@Builder
//@NoArgsConstructor
//@AllArgsConstructor
//@IdClass(UserRoleId.class)
//public class UserRole {
//
//    @Id
//    @ManyToOne
//    @JoinColumn(name = "user_id", nullable = false)
//    private User user;
//
//    @Id
//    @ManyToOne
//    @JoinColumn(name = "role_id", nullable = false)
//    private Role role;
//
//    @Column(name = "created_at")
//    private LocalDateTime createdAt;
//
//}
