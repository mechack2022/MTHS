package com.mths.payment.webhook;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

/**
 * Template Method Pattern: Base class for webhook processors
 * Provides common functionality for all webhook event processors
 */
@Slf4j
@RequiredArgsConstructor
public abstract class AbstractWebhookEventProcessor implements WebhookEventProcessor {

    @Override
    public void process(Map<String, Object> webhookData) {
        String eventType = getEventType();
        log.info("Processing webhook event: {}", eventType);

        try {
            Map<String, Object> data = extractData(webhookData);

            if (data == null || data.isEmpty()) {
                log.warn("No data found in webhook for event: {}", eventType);
                return;
            }

            processEventData(data);

            log.info("Successfully processed webhook event: {}", eventType);

        } catch (Exception e) {
            log.error("Error processing webhook event: {}, Error: {}",
                    eventType, e.getMessage(), e);
            handleError(e);
        }
    }

    /**
     * Extract data from webhook payload
     */
    @SuppressWarnings("unchecked")
    protected Map<String, Object> extractData(Map<String, Object> webhookData) {
        return (Map<String, Object>) webhookData.get("data");
    }

    /**
     * Extract value from nested map safely
     */
    @SuppressWarnings("unchecked")
    protected <T> T extractValue(Map<String, Object> map, String... keys) {
        Object current = map;
        for (String key : keys) {
            if (current instanceof Map) {
                current = ((Map<String, Object>) current).get(key);
            } else {
                return null;
            }
        }
        return (T) current;
    }

    /**
     * Process the actual event data (must be implemented by subclasses)
     */
    protected abstract void processEventData(Map<String, Object> data);

    /**
     * Handle errors during processing (can be overridden by subclasses)
     */
    protected void handleError(Exception e) {
        // Default: just log the error
        // Subclasses can override to implement retry logic, dead letter queue, etc.
    }
}
