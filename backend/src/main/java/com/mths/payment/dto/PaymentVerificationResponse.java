package com.mths.payment.dto;

import com.mths.payment.entity.Payment;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for payment verification response
 * Uses Builder pattern for flexible object construction
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PaymentVerificationResponse {

    private String reference;
    private Payment.PaymentStatus paymentStatus;
    private Long appointmentId;
    private String appointmentStatus;
    private String message;

    /**
     * Factory method for successful verification
     */
    public static PaymentVerificationResponse success(
            String reference,
            Long appointmentId,
            String appointmentStatus) {

        return PaymentVerificationResponse.builder()
                .reference(reference)
                .paymentStatus(Payment.PaymentStatus.COMPLETED)
                .appointmentId(appointmentId)
                .appointmentStatus(appointmentStatus)
                .message(appointmentId != null
                        ? "Payment verified successfully. Appointment created and awaiting doctor confirmation."
                        : "Payment verified. Creating appointment...")
                .build();
    }

    /**
     * Factory method for pending payment
     */
    public static PaymentVerificationResponse pending(String reference) {
        return PaymentVerificationResponse.builder()
                .reference(reference)
                .paymentStatus(Payment.PaymentStatus.PENDING)
                .message("Payment is still pending. Please complete the payment.")
                .build();
    }

    /**
     * Factory method for failed payment
     */
    public static PaymentVerificationResponse failed(String reference, String reason) {
        return PaymentVerificationResponse.builder()
                .reference(reference)
                .paymentStatus(Payment.PaymentStatus.FAILED)
                .message("Payment failed: " + reason)
                .build();
    }
}
