package com.auth.service.dto;

import com.auth.service.constants.UserType;

import java.util.Set;

//public record NewUserRecord(String username, String email, String password, String firstName, String lastName, Set<UserType> userTypes) {
//
//

public record NewUserRecord(
        String username,
        String email,
        String password,
        String firstName,
        String lastName,
        Set<UserType> userTypes
) {
    // Constructor for single role (backward compatibility)
    public NewUserRecord(String username, String email, String password,
                         String firstName, String lastName, UserType userType) {
        this(username, email, password, firstName, lastName, Set.of(userType));
    }
}

