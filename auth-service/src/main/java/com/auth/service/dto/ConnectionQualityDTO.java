package com.auth.service.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.Map;

@Data
public class ConnectionQualityDTO {
    private Long consultationId;
    private String overallQuality; // EXCELLENT, GOOD, FAIR, POOR
    private Integer qualityScore; // 1-10
    
    // Video metrics
    private Integer videoResolution;
    private Integer videoFrameRate;
    private Integer videoBitrate;
    private Integer videoPacketLoss;
    
    // Audio metrics
    private Integer audioSampleRate;
    private Integer audioBitrate;
    private Integer audioPacketLoss;
    private Integer audioLatency;
    
    // Network metrics
    private Integer bandwidth;
    private Integer latency; // RTT in ms
    private Integer jitter;
    
    // Participant quality
    private Map<String, ParticipantQuality> participantQualities;
    
    private LocalDateTime lastUpdated;
    private String recommendations;
    
    @Data
    public static class ParticipantQuality {
        private Long userId;
        private String userType;
        private String connectionType; // WIFI, MOBILE, ETHERNET
        private String quality; // EXCELLENT, GOOD, FAIR, POOR
        private Integer score; // 1-10
        private Map<String, Object> metrics;
    }
}