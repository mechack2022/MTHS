package com.mths.payment.webhook;

import java.util.Map;

/**
 * Strategy Pattern: Webhook Event Processor Interface
 * Each implementation handles a specific webhook event type
 */
public interface WebhookEventProcessor {

    /**
     * Get the event type this processor handles
     * (e.g., "charge.success", "charge.failed")
     */
    String getEventType();

    /**
     * Process the webhook event
     *
     * @param webhookData The webhook payload data
     */
    void process(Map<String, Object> webhookData);

    /**
     * Check if this processor can handle the given event type
     */
    default boolean canProcess(String eventType) {
        return getEventType().equalsIgnoreCase(eventType);
    }
}
