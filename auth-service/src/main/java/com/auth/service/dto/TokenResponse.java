package com.auth.service.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class TokenResponse {

    @JsonProperty("access_token")
    private String accessToken;

    @JsonProperty("refresh_token")
    private String refreshToken;

//    @JsonProperty("id_token")
//    private String idToken;

//    @JsonProperty("token_type")
//    private String tokenType;

    @JsonProperty("expires_in")
    private int expiresIn;

//    @JsonProperty("refresh_expires_in")
//    private int refreshExpiresIn;
}
