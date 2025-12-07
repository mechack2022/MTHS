package com.mths.payment.provider;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mths.payment.dto.PaymentInitializationResponse;
import com.mths.payment.entity.Payment;
import com.mths.payment.exception.PaymentInitializationException;
import com.mths.payment.exception.PaymentVerificationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Paystack Payment Provider Implementation
 * Handles all Paystack-specific payment operations
 */
@Slf4j
@Component
public class PaystackPaymentProvider extends AbstractPaymentProvider {

    private static final String PROVIDER_NAME = "PAYSTACK";
    private static final String HMAC_ALGORITHM = "HmacSHA512";

    @Value("${payment.paystack.secret-key}")
    private String secretKey;

    @Value("${payment.paystack.webhook-secret}")
    private String webhookSecret;

    @Value("${payment.paystack.base-url}")
    private String baseUrl;

    @Value("${payment.paystack.callback-url}")
    private String callbackUrl;

    public PaystackPaymentProvider(RestTemplate restTemplate, ObjectMapper objectMapper) {
        super(restTemplate, objectMapper);
    }

    @Override
    public String getProviderName() {
        return PROVIDER_NAME;
    }

    @Override
    public PaymentInitializationResponse initializePayment(Payment payment, Map<String, Object> metadata) {
        try {
            String url = baseUrl + "/transaction/initialize";

            Map<String, Object> requestBody = buildInitializationRequest(payment, metadata);

            logPaymentAction("INITIALIZE", payment.getReference(), "STARTING");

            ResponseEntity<Map> response = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    createAuthEntity(requestBody),
                    Map.class
            );

            return processInitializationResponse(response, payment);

        } catch (Exception e) {
            logPaymentError("INITIALIZE", payment.getReference(), e);
            throw new PaymentInitializationException(
                    "Failed to initialize payment with Paystack: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean verifyPayment(String reference) {
        try {
            String url = baseUrl + "/transaction/verify/" + reference;

            logPaymentAction("VERIFY", reference, "STARTING");

            ResponseEntity<Map> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    createAuthEntity(null),
                    Map.class
            );

            return processVerificationResponse(response, reference);

        } catch (Exception e) {
            logPaymentError("VERIFY", reference, e);
            throw new PaymentVerificationException(
                    "Failed to verify payment with Paystack: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean verifyWebhookSignature(String payload, String signature) {
        try {
            log.debug("[{}] Webhook Secret being used: {}", PROVIDER_NAME, webhookSecret);
            String computedSignature = computeHmacSignature(payload, webhookSecret);
            boolean isValid = computedSignature.equals(signature);

            log.info("[{}] Webhook signature verification: {}", PROVIDER_NAME,
                    isValid ? "VALID" : "INVALID");
            log.debug("[{}] Computed signature: {}", PROVIDER_NAME, computedSignature);
            log.debug("[{}] Received signature: {}", PROVIDER_NAME, signature);

            return isValid;

        } catch (Exception e) {
            log.error("[{}] Webhook signature verification failed: {}", PROVIDER_NAME, e.getMessage());
            return false;
        }
    }

    @Override
    public boolean initiateRefund(String reference, BigDecimal amount, String reason) {
        try {
            String url = baseUrl + "/refund";

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("transaction", reference);
            requestBody.put("amount", convertToKobo(amount));
            requestBody.put("merchant_note", reason);

            logPaymentAction("REFUND", reference, "INITIATING");

            ResponseEntity<Map> response = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    createAuthEntity(requestBody),
                    Map.class
            );

            boolean success = Optional.ofNullable(response.getBody())
                    .map(body -> (Boolean) body.get("status"))
                    .orElse(false);

            logPaymentAction("REFUND", reference, success ? "SUCCESS" : "FAILED");

            return success;

        } catch (Exception e) {
            logPaymentError("REFUND", reference, e);
            return false;
        }
    }

    @Override
    public boolean isEnabled() {
        return secretKey != null && !secretKey.isEmpty() && !secretKey.contains("your_secret_key");
    }

    @Override
    protected String getSecretKey() {
        return secretKey;
    }

    @Override
    protected String getBaseUrl() {
        return baseUrl;
    }

    // ========================================================================
    // PRIVATE HELPER METHODS
    // ========================================================================

    private Map<String, Object> buildInitializationRequest(Payment payment, Map<String, Object> metadata) {
        Map<String, Object> request = new HashMap<>();
        request.put("amount", convertToKobo(payment.getAmount()));
        request.put("email", payment.getPatientEmail());
        request.put("reference", payment.getReference());
        request.put("callback_url", callbackUrl);
        request.put("metadata", enrichMetadata(payment, metadata));

        return request;
    }

    private Map<String, Object> enrichMetadata(Payment payment, Map<String, Object> metadata) {
        Map<String, Object> enriched = new HashMap<>(metadata);
        enriched.put("patient_profile_id", payment.getPatientProfileId());
        enriched.put("doctor_profile_id", payment.getDoctorProfileId());
        enriched.put("scheduled_datetime", payment.getScheduledDatetime().toString());
        enriched.put("appointment_type", payment.getAppointmentType());

        return enriched;
    }

    private PaymentInitializationResponse processInitializationResponse(
            ResponseEntity<Map> response, Payment payment) {

        Map<String, Object> responseBody = Optional.ofNullable(response.getBody())
                .orElseThrow(() -> new PaymentInitializationException("Empty response from Paystack"));

        Boolean status = (Boolean) responseBody.get("status");
        if (status == null || !status) {
            String message = (String) responseBody.get("message");
            throw new PaymentInitializationException("Paystack initialization failed: " + message);
        }

        String authorizationUrl = extractFromMap(responseBody, "data", "authorization_url");
        if (authorizationUrl == null) {
            throw new PaymentInitializationException("No authorization URL in Paystack response");
        }

        logPaymentAction("INITIALIZE", payment.getReference(), "SUCCESS");

        return PaymentInitializationResponse.success(
                payment.getReference(),
                authorizationUrl,
                payment.getAmount(),
                "NGN"
        );
    }

    private boolean processVerificationResponse(ResponseEntity<Map> response, String reference) {
        Map<String, Object> responseBody = Optional.ofNullable(response.getBody())
                .orElse(Map.of());

        Boolean status = (Boolean) responseBody.get("status");
        if (status == null || !status) {
            logPaymentAction("VERIFY", reference, "FAILED - API returned false");
            return false;
        }

        String paymentStatus = extractFromMap(responseBody, "data", "status");
        boolean isSuccess = "success".equalsIgnoreCase(paymentStatus);

        logPaymentAction("VERIFY", reference, isSuccess ? "SUCCESS" : "FAILED - Status: " + paymentStatus);

        return isSuccess;
    }

    private long convertToKobo(BigDecimal amount) {
        return amount.multiply(BigDecimal.valueOf(100)).longValue();
    }

    private String computeHmacSignature(String payload, String secret) throws Exception {
        Mac mac = Mac.getInstance(HMAC_ALGORITHM);
        SecretKeySpec secretKeySpec = new SecretKeySpec(
                secret.getBytes(StandardCharsets.UTF_8),
                HMAC_ALGORITHM
        );
        mac.init(secretKeySpec);

        byte[] hash = mac.doFinal(payload.getBytes(StandardCharsets.UTF_8));

        return bytesToHex(hash);
    }

    private String bytesToHex(byte[] bytes) {
        StringBuilder hexString = new StringBuilder();
        for (byte b : bytes) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }
        return hexString.toString();
    }
}
