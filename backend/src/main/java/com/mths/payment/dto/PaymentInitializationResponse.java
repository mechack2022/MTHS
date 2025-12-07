package com.mths.payment.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * DTO for payment initialization response
 * Uses Builder pattern for flexible object construction
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PaymentInitializationResponse {

    private String reference;
    private String authorizationUrl; // URL patient should visit to pay
    private BigDecimal amount;
    private String currency;
    private String message;

    /**
     * Factory method for successful initialization
     */
    public static PaymentInitializationResponse success(
            String reference,
            String authorizationUrl,
            BigDecimal amount,
            String currency) {

        return PaymentInitializationResponse.builder()
                .reference(reference)
                .authorizationUrl(authorizationUrl)
                .amount(amount)
                .currency(currency)
                .message("Payment initialized successfully. Please complete payment to confirm your appointment.")
                .build();
    }
}
