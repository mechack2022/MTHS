package com.mths.auth.dto;

public class UserRegistrationResponse {
    private String userUuid;
    private String email;

    public UserRegistrationResponse() {}

    public UserRegistrationResponse(String userUuid, String email) {
        this.userUuid = userUuid;
        this.email = email;
    }

    public String getUserUuid() {
        return userUuid;
    }

    public void setUserUuid(String userUuid) {
        this.userUuid = userUuid;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}