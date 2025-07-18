package com.auth.service.dto;
//import com.fasterxml.jackson.annotation.JsonInclude;
//import lombok.*;
//
//import java.time.LocalDateTime;
//
//
//@NoArgsConstructor
//@JsonInclude(JsonInclude.Include.NON_NULL)
//@Builder
//@Getter
//@Setter
//@AllArgsConstructor
//public class ApiResponse<T> {
//
//    private boolean success;
//    private String message;
//    private T data;
//    private LocalDateTime timestamp = LocalDateTime.now();
//    private String error;
//
//    public static <T> ApiResponse<T> success(T data) {
//        return new ApiResponse<>(true, "Success", data, null);
//    }
//
//    public static <T> ApiResponse<T> success(T data, String message) {
//        return new ApiResponse<>(true, message, data,  null);
//    }
//
//    public static <T> ApiResponse<T> success(String message, T data) {
//        return new ApiResponse<>(true, message, data, null);
//    }
//
//    public static <T> ApiResponse<T> error(String message) {
//        return new ApiResponse<>(false, message, null, message);
//    }
//
//    public static <T> ApiResponse<T> error(String message, String detailedError) {
//        return new ApiResponse<>(false, message, null, detailedError);
//    }
//
//    public static <T> ApiResponse<T> error(String message, String detailedError, T data) {
//        return new ApiResponse<>(false, message, data, detailedError);
//    }
//
//    // Internal constructor
//    private ApiResponse(boolean success, String message, T data, String error) {
//        this.success = success;
//        this.message = message;
//        this.data = data;
//        this.error = error;
//        this.timestamp = LocalDateTime.now();
//    }
//
//}


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

    // Success response with only message
    public static <T> ApiResponse<T> success(String message) {
        return ApiResponse.<T>builder()
                .success(true)
                .message(message)
                .statusCode(200)
                .timestamp(LocalDateTime.now())
                .build();
    }

    // Error response
    public static <T> ApiResponse<T> error(String error, int statusCode) {
        return ApiResponse.<T>builder()
                .success(false)
                .error(error)
                .statusCode(statusCode)
                .timestamp(LocalDateTime.now())
                .build();
    }

    // Error response with message
    public static <T> ApiResponse<T> error(String message, String error, int statusCode) {
        return ApiResponse.<T>builder()
                .success(false)
                .message(message)
                .error(error)
                .statusCode(statusCode)
                .timestamp(LocalDateTime.now())
                .build();
    }
}


