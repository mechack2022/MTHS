package com.mths.payment.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mths.payment.provider.PaymentProvider;
import com.mths.payment.webhook.WebhookProcessorService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Webhook Controller
 * Handles webhook callbacks from payment providers (Paystack)
 *
 * Important:
 * - Must respond quickly (< 2 seconds) to avoid timeouts
 * - Must verify webhook signature for security
 * - Must be idempotent (handle duplicate webhooks gracefully)
 *
 * Endpoint:
 * - POST /api/webhooks/paystack - Receive Paystack webhooks
 */
@RestController
@RequestMapping("/api/webhooks")
@Slf4j
@RequiredArgsConstructor
public class WebhookController {

    private final PaymentProvider paymentProvider;
    private final WebhookProcessorService webhookProcessorService;
    private final ObjectMapper objectMapper;

    /**
     * Handle Paystack webhook events
     *
     * Security Flow:
     * 1. Verify webhook signature (prevents fake webhooks)
     * 2. Parse webhook payload
     * 3. Route to appropriate processor
     * 4. Return 200 OK quickly
     *
     * @param payload Raw webhook payload (for signature verification)
     * @param signature Paystack signature header
     * @return HTTP 200 if successful
     */
    @PostMapping("/paystack")
    public ResponseEntity<String> handlePaystackWebhook(
            @RequestBody String payload,
            @RequestHeader(value = "x-paystack-signature", required = false) String signature) {

        log.info("Received Paystack webhook");
        log.debug("Webhook payload: {}", payload);
        log.debug("Webhook signature: {}", signature);

        // Step 1: Verify webhook signature (Security!)
        if (signature == null || signature.isEmpty()) {
            log.error("Webhook signature missing");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Missing signature");
        }

        if (!paymentProvider.verifyWebhookSignature(payload, signature)) {
            log.error("Invalid webhook signature - Expected signature does not match");
            log.error("Received signature: {}", signature);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid signature");
        }

        log.info("Webhook signature verified successfully");

        try {
            // Step 2: Parse webhook payload
            Map<String, Object> webhookData = objectMapper.readValue(payload, Map.class);
            String eventType = (String) webhookData.get("event");

            if (eventType == null || eventType.isEmpty()) {
                log.error("No event type in webhook");
                return ResponseEntity.badRequest().body("Missing event type");
            }

            log.info("Processing webhook event type: {}", eventType);

            // Step 3: Process webhook using Strategy pattern
            // WebhookProcessorService will route to the right processor
            webhookProcessorService.processWebhook(eventType, webhookData);

            // Step 4: Return 200 OK quickly (Paystack expects response < 2 seconds)
            log.info("Webhook processed successfully: {}", eventType);
            return ResponseEntity.ok("Webhook received");

        } catch (Exception e) {
            log.error("Error processing webhook: {}", e.getMessage(), e);

            // Return 200 anyway to prevent Paystack from retrying for parsing errors
            // Only return non-200 for signature failures
            return ResponseEntity.ok("Webhook received but processing failed");
        }
    }

    /**
     * Health check endpoint for webhook
     */
    @GetMapping("/paystack/health")
    public ResponseEntity<String> webhookHealth() {
        return ResponseEntity.ok("Webhook endpoint is healthy");
    }
}
