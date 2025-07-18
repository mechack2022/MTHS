package com.auth.service.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class VerificationRequestDTO {
    private String code;
    private String userId;
}
