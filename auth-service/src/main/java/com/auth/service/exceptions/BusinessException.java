package com.auth.service.exceptions;



import lombok.Getter;

@Getter
public class BusinessException extends RuntimeException {

    private final String details;
    private final int statusCode;

    public BusinessException(String message) {
        super(message);
        this.details = message;
        this.statusCode = 400;
    }

    public BusinessException(String message, String details) {
        super(message);
        this.details = details;
        this.statusCode = 400;
    }

    public BusinessException(String message, int statusCode) {
        super(message);
        this.details = message;
        this.statusCode = statusCode;
    }

    public BusinessException(String message, String details, int statusCode) {
        super(message);
        this.details = details;
        this.statusCode = statusCode;
    }

    public BusinessException(String message, Throwable cause) {
        super(message, cause);
        this.details = message;
        this.statusCode = 400;
    }

    public BusinessException(String message, String details, int statusCode, Throwable cause) {
        super(message, cause);
        this.details = details;
        this.statusCode = statusCode;
    }
}
