package com.auth.service.dto;

import com.auth.service.entity.User;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDateTime;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserDTO {
//    private String uuid;
//    private String email;
//    private String firstName;
//    private String lastName;
//    private Boolean mailVerified;
//    private String accountType;
//    private Boolean isActive;
//    private Boolean accountVerified;
//    private LocalDateTime verificationCodeExpiry;
//    private LocalDateTime passwordResetCodeExpiry;
//    private Set<String> roles;
//    private Set<String> permissions;
//    private LocalDateTime createdAt;
//    private LocalDateTime updatedAt;

    private String uuid;

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;

    @NotBlank(message = "Password is required")
    private String password;

    @NotBlank(message = "First name is required")
    private String firstName;

    private String lastName;

    @NotNull(message = "Account type is required")
    private User.AccountType accountType;

    private Boolean mailVerified;
    private Boolean isActive;
    private Boolean accountVerified;
    private Set<String> roles;
    private Set<String> permissions;
}
