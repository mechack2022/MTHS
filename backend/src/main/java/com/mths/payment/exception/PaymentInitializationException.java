package com.mths.payment.exception;

/**
 * Thrown when payment initialization fails
 */
public class PaymentInitializationException extends PaymentException {

    public PaymentInitializationException(String message) {
        super(message);
    }

    public PaymentInitializationException(String message, Throwable cause) {
        super(message, cause);
    }
}
