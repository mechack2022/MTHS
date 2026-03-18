package com.mths.consultation.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.index.Indexed;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Redis entity for storing real-time video consultation session data
 * This is a temporary cache that gets flushed to PostgreSQL when consultation ends
 * TTL: 24 hours (sessions expire after 24 hours of inactivity)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@RedisHash(value = "consultation:session", timeToLive = 86400) // 24 hours TTL
public class VideoConsultationSession implements Serializable {

    @Id
    private String sessionId;

    @Indexed
    private Long consultationId; // Reference to PostgreSQL VideoConsultation entity

    @Indexed
    private String roomId;

    private Long appointmentId;
    private Long patientId;
    private Long doctorId;

    private String status; // SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED

    private LocalDateTime sessionStartTime;
    private LocalDateTime sessionEndTime;

    // Participant tracking
    private LocalDateTime patientJoinedAt;
    private LocalDateTime patientLeftAt;
    private LocalDateTime doctorJoinedAt;
    private LocalDateTime doctorLeftAt;

    private boolean patientCurrentlyConnected;
    private boolean doctorCurrentlyConnected;

    // Real-time events collected during the session
    @Builder.Default
    private List<ConsultationEvent> events = new ArrayList<>();

    // Chat messages during consultation (temporary until flushed)
    @Builder.Default
    private List<SessionChatMessage> chatMessages = new ArrayList<>();

    // Media state tracking
    @Builder.Default
    private Map<String, MediaState> participantMediaStates = new HashMap<>();

    // Connection quality metrics
    @Builder.Default
    private List<ConnectionQualityMetric> qualityMetrics = new ArrayList<>();

    // WebRTC signaling events (for debugging/analytics)
    @Builder.Default
    private List<WebRTCSignalEvent> signalingEvents = new ArrayList<>();

    // Screen sharing events
    @Builder.Default
    private List<ScreenShareEvent> screenShareEvents = new ArrayList<>();

    // Metadata
    private LocalDateTime createdAt;
    private LocalDateTime lastUpdatedAt;
    private boolean flushedToDatabase;
    private LocalDateTime flushedAt;

    /**
     * Represents a consultation event (participant joined, left, etc.)
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ConsultationEvent implements Serializable {
        private String eventType; // PARTICIPANT_JOINED, PARTICIPANT_LEFT, STATUS_CHANGED
        private Long userId;
        private String userType; // PATIENT, DOCTOR
        private String eventData;
        private LocalDateTime timestamp;
    }

    /**
     * Represents a chat message during the session
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class SessionChatMessage implements Serializable {
        private String messageId;
        private Long senderId;
        private String senderType;
        private String content;
        private String messageType; // TEXT, FILE, SYSTEM
        private LocalDateTime sentAt;
        private LocalDateTime deliveredAt;
        private LocalDateTime readAt;
        private String attachmentUrl;
        private String attachmentType;
    }

    /**
     * Represents media state for a participant
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class MediaState implements Serializable {
        private Long userId;
        private String userType;
        private boolean videoEnabled;
        private boolean audioEnabled;
        private boolean screenSharing;
        private LocalDateTime lastUpdated;
    }

    /**
     * Represents connection quality metrics
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ConnectionQualityMetric implements Serializable {
        private Long userId;
        private String userType;
        private Integer packetLoss;
        private Integer jitter;
        private Integer roundTripTime;
        private Integer bandwidth;
        private String qualityRating; // EXCELLENT, GOOD, FAIR, POOR
        private LocalDateTime timestamp;
    }

    /**
     * Represents WebRTC signaling events
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class WebRTCSignalEvent implements Serializable {
        private String eventType; // OFFER, ANSWER, ICE_CANDIDATE
        private Long senderId;
        private Long targetUserId;
        private String signalData;
        private LocalDateTime timestamp;
    }

    /**
     * Represents screen sharing events
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ScreenShareEvent implements Serializable {
        private String eventType; // STARTED, STOPPED
        private Long userId;
        private String userType;
        private LocalDateTime timestamp;
    }

    // Helper methods

    public void addEvent(String eventType, Long userId, String userType, String eventData) {
        if (this.events == null) {
            this.events = new ArrayList<>();
        }
        this.events.add(ConsultationEvent.builder()
            .eventType(eventType)
            .userId(userId)
            .userType(userType)
            .eventData(eventData)
            .timestamp(LocalDateTime.now())
            .build());
        this.lastUpdatedAt = LocalDateTime.now();
    }

    public void addChatMessage(SessionChatMessage message) {
        if (this.chatMessages == null) {
            this.chatMessages = new ArrayList<>();
        }
        this.chatMessages.add(message);
        this.lastUpdatedAt = LocalDateTime.now();
    }

    public void updateMediaState(Long userId, String userType, boolean videoEnabled, boolean audioEnabled) {
        if (this.participantMediaStates == null) {
            this.participantMediaStates = new HashMap<>();
        }
        String key = userId + "_" + userType;
        this.participantMediaStates.put(key, MediaState.builder()
            .userId(userId)
            .userType(userType)
            .videoEnabled(videoEnabled)
            .audioEnabled(audioEnabled)
            .lastUpdated(LocalDateTime.now())
            .build());
        this.lastUpdatedAt = LocalDateTime.now();
    }

    public void addQualityMetric(ConnectionQualityMetric metric) {
        if (this.qualityMetrics == null) {
            this.qualityMetrics = new ArrayList<>();
        }
        this.qualityMetrics.add(metric);
        this.lastUpdatedAt = LocalDateTime.now();
    }

    public void addSignalingEvent(WebRTCSignalEvent event) {
        if (this.signalingEvents == null) {
            this.signalingEvents = new ArrayList<>();
        }
        this.signalingEvents.add(event);
        this.lastUpdatedAt = LocalDateTime.now();
    }

    public void addScreenShareEvent(String eventType, Long userId, String userType) {
        if (this.screenShareEvents == null) {
            this.screenShareEvents = new ArrayList<>();
        }
        this.screenShareEvents.add(ScreenShareEvent.builder()
            .eventType(eventType)
            .userId(userId)
            .userType(userType)
            .timestamp(LocalDateTime.now())
            .build());
        this.lastUpdatedAt = LocalDateTime.now();
    }

    public boolean isActive() {
        return "IN_PROGRESS".equals(status) && sessionEndTime == null;
    }

    public boolean shouldFlush() {
        return "COMPLETED".equals(status) || "CANCELLED".equals(status);
    }
}
