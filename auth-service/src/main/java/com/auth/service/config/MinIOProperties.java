package com.auth.service.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

@Configuration
@ConfigurationProperties(prefix = "minio")
@Data
public class MinIOProperties {
    private String endpoint;
    private String accessKey;
    private String secretKey;
    private Map<String, String> buckets = new HashMap<>();
}
