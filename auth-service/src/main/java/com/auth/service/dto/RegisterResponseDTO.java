package com.auth.service.dto;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RegisterResponseDTO {
    private Long id;
    private String email;
    private String fullName;
    private String accountType;
    private LocalDateTime registeredAt;
}
