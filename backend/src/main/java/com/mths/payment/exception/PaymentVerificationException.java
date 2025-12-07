package com.mths.payment.exception;

/**
 * Thrown when payment verification fails
 */
public class PaymentVerificationException extends PaymentException {

    public PaymentVerificationException(String message) {
        super(message);
    }

    public PaymentVerificationException(String message, Throwable cause) {
        super(message, cause);
    }
}
