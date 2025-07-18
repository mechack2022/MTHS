package com.auth.service.entity;//package com.digi_dokita.entity;
//
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
//@Table(name = "auth_tokens")
//@Setter
//@Getter
//@Builder
//@NoArgsConstructor
//@AllArgsConstructor
//public class AuthenticationToken {
//
//    @Id
//    @GeneratedValue(strategy = GenerationType.IDENTITY) // or AUTO
//    private Long id;
//
//    @ManyToOne
//    @JoinColumn(name = "user_id", nullable = false)
//    private User user;
//
//    @Column(name = "token_value", nullable = false)
//    private String tokenValue;
//
//    @Column(name = "expires_at", nullable = false)
//    private LocalDateTime expiresAt;
//
//    @Column(name = "created_at", nullable = false)
//    private LocalDateTime createdAt;
//}
