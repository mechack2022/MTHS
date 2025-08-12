package com.auth.service.exceptions;

public class BadRequestException extends BusinessException {
    private final String field;
    private final String message;

    public BadRequestException(String field, String message) {
        super(message);
        this.field = field;
        this.message = message;
    }

    public String getField() {
        return field;
    }

    @Override
    public String getMessage() {
        return message;
    }
}