package com.mths.payment.provider;

import com.mths.payment.dto.PaymentInitializationResponse;
import com.mths.payment.entity.Payment;

import java.math.BigDecimal;
import java.util.Map;

/**
 * Strategy Pattern: Payment Provider Interface
 * Allows different payment providers (Paystack, Flutterwave, etc.) to be plugged in
 */
public interface PaymentProvider {

    /**
     * Get the provider name (e.g., "PAYSTACK", "FLUTTERWAVE")
     */
    String getProviderName();

    /**
     * Initialize a payment transaction
     *
     * @param payment The payment entity to initialize
     * @param metadata Additional metadata for the payment
     * @return Payment initialization response with authorization URL
     */
    PaymentInitializationResponse initializePayment(Payment payment, Map<String, Object> metadata);

    /**
     * Verify a payment transaction
     *
     * @param reference Payment reference to verify
     * @return true if payment is successful, false otherwise
     */
    boolean verifyPayment(String reference);

    /**
     * Verify webhook signature for security
     *
     * @param payload Raw webhook payload
     * @param signature Signature header from webhook
     * @return true if signature is valid
     */
    boolean verifyWebhookSignature(String payload, String signature);

    /**
     * Process refund for a payment
     *
     * @param reference Payment reference to refund
     * @param amount Amount to refund
     * @param reason Reason for refund
     * @return true if refund initiated successfully
     */
    boolean initiateRefund(String reference, BigDecimal amount, String reason);

    /**
     * Check if this provider is enabled
     */
    boolean isEnabled();
}
