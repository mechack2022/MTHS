package com.mths.payment.webhook;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Webhook Processor Service - Routes webhook events to appropriate processors
 * Demonstrates:
 * - Strategy Pattern: Different processors for different events
 * - Dependency Injection: All processors injected via List
 * - Collection usage: List.stream() for finding the right processor
 */
@Slf4j
@Service
public class WebhookProcessorService {

    private final List<WebhookEventProcessor> processors;

    /**
     * Constructor injection with List of all WebhookEventProcessor implementations
     * Spring automatically injects all beans implementing the interface
     */
    public WebhookProcessorService(List<WebhookEventProcessor> processors) {
        this.processors = processors;
        log.info("Initialized WebhookProcessorService with {} processors", processors.size());

        // Log available processors
        processors.forEach(processor ->
                log.info("Registered webhook processor: {} for event: {}",
                        processor.getClass().getSimpleName(),
                        processor.getEventType())
        );
    }

    /**
     * Process webhook event by routing to appropriate processor
     * Uses Java Streams for functional programming
     *
     * @param eventType The type of webhook event
     * @param webhookData The webhook payload
     */
    public void processWebhook(String eventType, Map<String, Object> webhookData) {
        log.info("Received webhook event: {}", eventType);

        // Use Optional and Stream for elegant null handling and searching
        Optional<WebhookEventProcessor> processor = findProcessorForEvent(eventType);

        if (processor.isPresent()) {
            processor.get().process(webhookData);
        } else {
            log.warn("No processor found for event type: {}. Event ignored.", eventType);
        }
    }

    /**
     * Find processor for given event type using Java 8 Streams
     * Demonstrates functional programming in Java
     */
    private Optional<WebhookEventProcessor> findProcessorForEvent(String eventType) {
        return processors.stream()
                .filter(processor -> processor.canProcess(eventType))
                .findFirst(); // Returns Optional<WebhookEventProcessor>
    }

    /**
     * Get all registered event types (useful for debugging/monitoring)
     */
    public List<String> getRegisteredEventTypes() {
        return processors.stream()
                .map(WebhookEventProcessor::getEventType)
                .toList(); // Java 16+ feature
    }
}
