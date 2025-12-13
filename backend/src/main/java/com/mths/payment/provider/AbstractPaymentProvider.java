package com.mths.payment.provider;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

/**
 * Template Method Pattern: Base class for payment providers
 * Provides common functionality for all payment providers
 */
@Slf4j
@RequiredArgsConstructor
public abstract class AbstractPaymentProvider implements PaymentProvider {

    protected final RestTemplate restTemplate;
    protected final ObjectMapper objectMapper;

    /**
     * Create HTTP headers with authentication
     */
    protected HttpHeaders createAuthHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(getSecretKey());
        return headers;
    }

    /**
     * Create HTTP entity with body and auth headers
     */
    protected <T> HttpEntity<T> createAuthEntity(T body) {
        return new HttpEntity<>(body, createAuthHeaders());
    }

    /**
     * Log payment provider actions
     */
    protected void logPaymentAction(String action, String reference, String status) {
        log.info("[{}] {} - Reference: {}, Status: {}",
                getProviderName(), action, reference, status);
    }

    /**
     * Log payment provider errors
     */
    protected void logPaymentError(String action, String reference, Exception e) {
        log.error("[{}] {} failed - Reference: {}, Error: {}",
                getProviderName(), action, reference, e.getMessage(), e);
    }

    /**
     * Extract nested value from map safely
     */
    @SuppressWarnings("unchecked")
    protected <T> T extractFromMap(Map<String, Object> map, String... keys) {
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
     * Get provider secret key (must be implemented by subclasses)
     */
    protected abstract String getSecretKey();

    /**
     * Get provider base URL (must be implemented by subclasses)
     */
    protected abstract String getBaseUrl();
}
