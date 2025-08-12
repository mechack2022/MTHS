package com.auth.service.mapper;


import com.auth.service.dto.UserDTO;
import com.auth.service.entity.User;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.stream.Collectors;

@Component
public class UserMapper {

    public UserDTO toDto(User user) {
        if (user == null) {
            return null;
        }

        return UserDTO.builder()
                .uuid(user.getUuid())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .mailVerified(user.getMailVerified())
                .accountType(user.getAccountType() != null ? user.getAccountType() : null)
                .isActive(user.getIsActive())
                .accountVerified(user.getAccountVerified())
//                .verificationCodeExpiry(user.getVerificationCodeExpiry())
//                .passwordResetCodeExpiry(user.getPasswordResetCodeExpiry())
                .roles(extractRoleNames(user))
                .permissions(extractPermissionNames(user))
//                .createdAt(user.getCreatedAt())
//                .updatedAt(user.getUpdatedAt())
                .build();
    }

    private Set<String> extractRoleNames(User user) {
        return user.getRoles().stream()
                .map(role -> role.getRoleName().name())
                .collect(Collectors.toSet());
    }

    private Set<String> extractPermissionNames(User user) {
        return user.getAllPermissions().stream()
                .map(permission -> permission.getPermissionName().name())
                .collect(Collectors.toSet());
    }

    public User toEntity(UserDTO userDTO) {
        User user = new User();
        user.setUuid(userDTO.getUuid());
        user.setEmail(userDTO.getEmail());
        user.setFirstName(userDTO.getFirstName());
        user.setLastName(userDTO.getLastName());
        user.setMailVerified(userDTO.getMailVerified());
        user.setAccountType(userDTO.getAccountType() != null ?
                User.AccountType.valueOf(userDTO.getAccountType().name()) : null);
        user.setIsActive(userDTO.getIsActive());
        user.setAccountVerified(userDTO.getAccountVerified());
        return user;
    }
}