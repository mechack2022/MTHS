package com.mths.consultation.service;

import com.mths.consultation.entity.ChatMessage;
import com.mths.consultation.entity.VideoConsultation;
import com.mths.consultation.entity.VideoConsultationSession;
import com.mths.consultation.repository.ChatMessageRepository;
import com.mths.consultation.repository.VideoConsultationRepository;
import com.mths.consultation.repository.VideoConsultationSessionRepository;
import com.mths.shared.constants.ConsultationStatus;
import com.mths.shared.constants.MessageStatus;
import com.mths.shared.constants.MessageType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class VideoConsultationCacheService {

    private final VideoConsultationSessionRepository sessionRepository;
    private final VideoConsultationRepository videoConsultationRepository;
    private final ChatMessageRepository chatMessageRepository;

    public VideoConsultationSession initializeSession(VideoConsultation consultation) {
        log.info("Initializing Redis session for consultation: {}", consultation.getId());

        VideoConsultationSession session = VideoConsultationSession.builder()
            .sessionId(consultation.getSessionId())
            .consultationId(consultation.getId())
            .roomId(consultation.getRoomId())
            .appointmentId(consultation.getAppointmentId())
            .patientId(consultation.getPatientProfileId())
            .doctorId(consultation.getDoctorProfileId())
            .status(consultation.getStatus().name())
            .sessionStartTime(LocalDateTime.now())
            .patientCurrentlyConnected(false)
            .doctorCurrentlyConnected(false)
            .createdAt(LocalDateTime.now())
            .lastUpdatedAt(LocalDateTime.now())
            .flushedToDatabase(false)
            .build();

        return sessionRepository.save(session);
    }

    public Optional<VideoConsultationSession> getSession(String sessionId) {
        return sessionRepository.findById(sessionId);
    }

    public Optional<VideoConsultationSession> getSessionByConsultationId(Long consultationId) {
        return sessionRepository.findByConsultationId(consultationId);
    }

    public VideoConsultationSession updateSession(VideoConsultationSession session) {
        session.setLastUpdatedAt(LocalDateTime.now());
        return sessionRepository.save(session);
    }

    /**
     * Record participant join event
     */
    public void recordParticipantJoin(String sessionId, Long userId, String userType) {
        getSession(sessionId).ifPresent(session -> {
            LocalDateTime now = LocalDateTime.now();

            if ("PATIENT".equals(userType)) {
                session.setPatientJoinedAt(now);
                session.setPatientCurrentlyConnected(true);
            } else if ("DOCTOR".equals(userType)) {
                session.setDoctorJoinedAt(now);
                session.setDoctorCurrentlyConnected(true);
            }

            session.addEvent("PARTICIPANT_JOINED", userId, userType, null);

            // Update status to IN_PROGRESS if both participants are connected
            if (session.isPatientCurrentlyConnected() && session.isDoctorCurrentlyConnected()) {
                session.setStatus("IN_PROGRESS");
            }

            updateSession(session);
            log.info("Recorded participant join - Session: {}, User: {}, Type: {}", sessionId, userId, userType);
        });
    }

    /**
     * Record participant leave event
     */
    public void recordParticipantLeave(String sessionId, Long userId, String userType) {
        getSession(sessionId).ifPresent(session -> {
            LocalDateTime now = LocalDateTime.now();

            if ("PATIENT".equals(userType)) {
                session.setPatientLeftAt(now);
                session.setPatientCurrentlyConnected(false);
            } else if ("DOCTOR".equals(userType)) {
                session.setDoctorLeftAt(now);
                session.setDoctorCurrentlyConnected(false);
            }

            session.addEvent("PARTICIPANT_LEFT", userId, userType, null);

            updateSession(session);
            log.info("Recorded participant leave - Session: {}, User: {}, Type: {}", sessionId, userId, userType);
        });
    }

    /**
     * Record chat message to Redis
     */
    public void recordChatMessage(String sessionId, Long senderId, String senderType,
                                  String content, String messageType) {
        getSession(sessionId).ifPresent(session -> {
            VideoConsultationSession.SessionChatMessage message =
                VideoConsultationSession.SessionChatMessage.builder()
                    .messageId(UUID.randomUUID().toString())
                    .senderId(senderId)
                    .senderType(senderType)
                    .content(content)
                    .messageType(messageType != null ? messageType : "TEXT")
                    .sentAt(LocalDateTime.now())
                    .build();

            session.addChatMessage(message);
            updateSession(session);
            log.debug("Recorded chat message - Session: {}, Sender: {}", sessionId, senderId);
        });
    }

    /**
     * Record media state change (mute/unmute)
     */
    public void recordMediaStateChange(String sessionId, Long userId, String userType,
                                      boolean videoEnabled, boolean audioEnabled) {
        getSession(sessionId).ifPresent(session -> {
            session.updateMediaState(userId, userType, videoEnabled, audioEnabled);
            session.addEvent("MEDIA_STATE_CHANGED", userId, userType,
                String.format("video=%s,audio=%s", videoEnabled, audioEnabled));
            updateSession(session);
            log.debug("Recorded media state change - Session: {}, User: {}", sessionId, userId);
        });
    }

    /**
     * Record connection quality metrics
     */
    public void recordConnectionQuality(String sessionId, Long userId, String userType,
                                       Integer packetLoss, Integer jitter, Integer rtt,
                                       Integer bandwidth, String qualityRating) {
        getSession(sessionId).ifPresent(session -> {
            VideoConsultationSession.ConnectionQualityMetric metric =
                VideoConsultationSession.ConnectionQualityMetric.builder()
                    .userId(userId)
                    .userType(userType)
                    .packetLoss(packetLoss)
                    .jitter(jitter)
                    .roundTripTime(rtt)
                    .bandwidth(bandwidth)
                    .qualityRating(qualityRating)
                    .timestamp(LocalDateTime.now())
                    .build();

            session.addQualityMetric(metric);
            updateSession(session);
            log.debug("Recorded connection quality - Session: {}, User: {}, Quality: {}",
                sessionId, userId, qualityRating);
        });
    }

    /**
     * Record screen share event
     */
    public void recordScreenShare(String sessionId, Long userId, String userType, String eventType) {
        getSession(sessionId).ifPresent(session -> {
            session.addScreenShareEvent(eventType, userId, userType);
            updateSession(session);
            log.info("Recorded screen share {} - Session: {}, User: {}", eventType, sessionId, userId);
        });
    }

    /**
     * Record WebRTC signaling event
     */
    public void recordSignalingEvent(String sessionId, String eventType, Long senderId,
                                     Long targetUserId, String signalData) {
        getSession(sessionId).ifPresent(session -> {
            VideoConsultationSession.WebRTCSignalEvent event =
                VideoConsultationSession.WebRTCSignalEvent.builder()
                    .eventType(eventType)
                    .senderId(senderId)
                    .targetUserId(targetUserId)
                    .signalData(signalData)
                    .timestamp(LocalDateTime.now())
                    .build();

            session.addSignalingEvent(event);
            updateSession(session);
        });
    }

    /**
     * Mark session as completed and ready for flush
     */
    public void completeSession(String sessionId) {
        getSession(sessionId).ifPresent(session -> {
            session.setStatus("COMPLETED");
            session.setSessionEndTime(LocalDateTime.now());
            session.addEvent("SESSION_COMPLETED", null, null, null);
            updateSession(session);
            log.info("Session marked as completed: {}", sessionId);
        });
    }

    /**
     * Flush session data from Redis to PostgreSQL
     * This is called when consultation ends or via scheduled task
     */
    @Transactional
    public void flushSessionToDatabase(String sessionId) {
        Optional<VideoConsultationSession> sessionOpt = getSession(sessionId);
        if (sessionOpt.isEmpty()) {
            log.warn("Cannot flush session - not found in Redis: {}", sessionId);
            return;
        }

        VideoConsultationSession session = sessionOpt.get();
        if (session.isFlushedToDatabase()) {
            log.info("Session already flushed: {}", sessionId);
            return;
        }

        log.info("Flushing session to PostgreSQL: {}", sessionId);

        try {
            // Update VideoConsultation entity with session data
            VideoConsultation consultation = videoConsultationRepository.findById(session.getConsultationId())
                .orElseThrow(() -> new RuntimeException("Consultation not found: " + session.getConsultationId()));

            // Update timestamps and status
            if (session.getPatientJoinedAt() != null) {
                consultation.setPatientJoinedAt(session.getPatientJoinedAt());
            }
            if (session.getPatientLeftAt() != null) {
                consultation.setPatientLeftAt(session.getPatientLeftAt());
            }
            if (session.getDoctorJoinedAt() != null) {
                consultation.setDoctorJoinedAt(session.getDoctorJoinedAt());
            }
            if (session.getDoctorLeftAt() != null) {
                consultation.setDoctorLeftAt(session.getDoctorLeftAt());
            }

            if (session.getSessionEndTime() != null) {
                consultation.setEndTime(session.getSessionEndTime());
            }

            if ("COMPLETED".equals(session.getStatus())) {
                consultation.setStatus(ConsultationStatus.COMPLETED);
            }

            // Calculate duration
            if (session.getSessionStartTime() != null && session.getSessionEndTime() != null) {
                long duration = java.time.Duration.between(
                    session.getSessionStartTime(),
                    session.getSessionEndTime()
                ).toMinutes();
                consultation.setDurationMinutes((int) duration);
            }

            videoConsultationRepository.save(consultation);

            // Save chat messages to PostgreSQL
            if (session.getChatMessages() != null && !session.getChatMessages().isEmpty()) {
                for (VideoConsultationSession.SessionChatMessage msg : session.getChatMessages()) {
                    ChatMessage chatMessage = new ChatMessage();
                    chatMessage.setVideoConsultation(consultation);
                    chatMessage.setSenderId(msg.getSenderId());
                    chatMessage.setSenderType(msg.getSenderType());
                    // Set receiver based on sender type
                    if ("PATIENT".equals(msg.getSenderType())) {
                        chatMessage.setReceiverId(session.getDoctorId());
                        chatMessage.setReceiverType("DOCTOR");
                    } else {
                        chatMessage.setReceiverId(session.getPatientId());
                        chatMessage.setReceiverType("PATIENT");
                    }
                    chatMessage.setMessageType(MessageType.valueOf(msg.getMessageType()));
                    chatMessage.setContent(msg.getContent());
                    chatMessage.setSentAt(msg.getSentAt());
                    chatMessage.setDeliveredAt(msg.getDeliveredAt());
                    chatMessage.setReadAt(msg.getReadAt());
                    chatMessage.setStatus(MessageStatus.SENT);
                    if (msg.getAttachmentUrl() != null) {
                        chatMessage.setAttachmentUrl(msg.getAttachmentUrl());
                        chatMessage.setAttachmentType(msg.getAttachmentType());
                    }

                    chatMessageRepository.save(chatMessage);
                }
                log.info("Flushed {} chat messages to PostgreSQL", session.getChatMessages().size());
            }

            // Mark session as flushed
            session.setFlushedToDatabase(true);
            session.setFlushedAt(LocalDateTime.now());
            updateSession(session);

            log.info("Successfully flushed session {} to PostgreSQL", sessionId);

        } catch (Exception e) {
            log.error("Error flushing session {} to PostgreSQL", sessionId, e);
            throw new RuntimeException("Failed to flush session to database", e);
        }
    }

    @Transactional
    public void flushAllCompletedSessions() {
        log.info("Flushing all completed sessions to PostgreSQL");

        List<VideoConsultationSession> unflushedSessions = sessionRepository.findByFlushedToDatabase(false);
        log.info("Found {} unflushed sessions", unflushedSessions.size());

        int flushedCount = 0;
        for (VideoConsultationSession session : unflushedSessions) {
            if (session.shouldFlush()) {
                try {
                    flushSessionToDatabase(session.getSessionId());
                    flushedCount++;
                } catch (Exception e) {
                    log.error("Error flushing session {}", session.getSessionId(), e);
                }
            }
        }

        log.info("Flushed {} sessions to PostgreSQL", flushedCount);
    }

    public void deleteSession(String sessionId) {
        sessionRepository.deleteById(sessionId);
        log.info("Deleted session from Redis: {}", sessionId);
    }

    public List<VideoConsultationSession> getActiveSessions() {
        return sessionRepository.findByStatus("IN_PROGRESS");
    }
}
