package com.auth.service.dto;


import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
//
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {

    private boolean success;
    private String message;
    private T data;
    private String error;
    private int statusCode;
    private LocalDateTime timestamp;

    // Success response with data
    public static <T> ApiResponse<T> success(T data) {
        return ApiResponse.<T>builder()
                .success(true)
                .data(data)
                .statusCode(200)
                .timestamp(LocalDateTime.now())
                .build();
    }

    // Success response with data and custom message
    public static <T> ApiResponse<T> success(String message, T data) {
        return ApiResponse.<T>builder()
                .success(true)
                .message(message)
                .data(data)
                .statusCode(200)
                .timestamp(LocalDateTime.now())
                .build();
    }

    public static <T> ApiResponse<T> success(String message, T data, int statusCode) {
        return ApiResponse.<T>builder()
                .success(true)
                .message(message)
                .data(data)
                .statusCode(statusCode)
                .timestamp(LocalDateTime.now())
                .build();
    }

    // Success response with only message
    public static <T> ApiResponse<T> success(String message) {
        return ApiResponse.<T>builder()
                .success(true)
                .message(message)
                .statusCode(200)
                .timestamp(LocalDateTime.now())
                .build();
    }

    // Error response - specific error details go in message, generic error in error field
    public static <T> ApiResponse<T> error(String specificErrorDetails, int statusCode) {
        return ApiResponse.<T>builder()
                .success(false)
                .message(specificErrorDetails)
                .error("An error occurred while processing your request")
                .statusCode(statusCode)
                .timestamp(LocalDateTime.now())
                .build();
    }

    // Error response with custom generic error message
    public static <T> ApiResponse<T> error(String specificErrorDetails, String genericErrorMessage, int statusCode) {
        return ApiResponse.<T>builder()
                .success(false)
                .message(specificErrorDetails)
                .error(genericErrorMessage)
                .statusCode(statusCode)
                .timestamp(LocalDateTime.now())
                .build();
    }
}


