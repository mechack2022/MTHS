package com.mths.payment.config;

import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;

/**
 * Payment Configuration
 * Configures beans needed for payment processing
 */
@Configuration
public class PaymentConfig {

    /**
     * RestTemplate bean for making HTTP requests to payment providers
     * Configured with timeouts for reliability
     */
    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder builder) {
        return builder
                .setConnectTimeout(Duration.ofSeconds(10)) // Connection timeout
                .setReadTimeout(Duration.ofSeconds(30))     // Read timeout
                .build();
    }
}
