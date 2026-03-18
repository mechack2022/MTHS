package com.mths.shared.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mths.consultation.service.VideoConsultationCacheService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.*;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
@Slf4j
@RequiredArgsConstructor
public class VideoConsultationWebSocketHandler implements WebSocketHandler {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final VideoConsultationCacheService cacheService;
    
    // Store active sessions: userId -> WebSocketSession
    private final Map<String, WebSocketSession> activeSessions = new ConcurrentHashMap<>();
    
    // Store user consultation mapping: userId -> consultationId
    private final Map<String, String> userConsultationMapping = new ConcurrentHashMap<>();
    
    // Store consultation participants: consultationId -> Set<userId>
    private final Map<String, Map<String, WebSocketSession>> consultationParticipants = new ConcurrentHashMap<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        log.info("Video consultation WebSocket connection established: {}", session.getId());
        
        String userId = extractUserIdFromSession(session);
        String consultationId = extractConsultationIdFromSession(session);
        
        if (userId != null && consultationId != null) {
            activeSessions.put(userId, session);
            userConsultationMapping.put(userId, consultationId);
            
            // Add to consultation participants
            consultationParticipants.computeIfAbsent(consultationId, k -> new ConcurrentHashMap<>())
                    .put(userId, session);
            
            // Record participant join in Redis
            cacheService.recordParticipantJoin(consultationId, Long.parseLong(userId),
                extractUserType(userId, consultationId));

            // Notify other participants about new participant
            broadcastToConsultation(consultationId, Map.of(
                "type", "PARTICIPANT_JOINED",
                "userId", userId,
                "consultationId", consultationId,
                "timestamp", System.currentTimeMillis()
            ), userId);

            // Send welcome message with current participants
            sendParticipantsList(session, consultationId);

            log.info("User {} joined video consultation {} via WebSocket", userId, consultationId);
        }
    }

    @Override
    public void handleMessage(WebSocketSession session, WebSocketMessage<?> message) throws Exception {
        try {
            String payload = (String) message.getPayload();
            Map<String, Object> messageData = objectMapper.readValue(payload, Map.class);
            
            String messageType = (String) messageData.get("type");
            String userId = extractUserIdFromSession(session);
            String consultationId = userConsultationMapping.get(userId);
            
            switch (messageType) {
                case "WEBRTC_OFFER":
                    handleWebRTCOffer(userId, consultationId, messageData);
                    break;
                case "WEBRTC_ANSWER":
                    handleWebRTCAnswer(userId, consultationId, messageData);
                    break;
                case "ICE_CANDIDATE":
                    handleICECandidate(userId, consultationId, messageData);
                    break;
                case "MEDIA_STATE_CHANGE":
                    handleMediaStateChange(userId, consultationId, messageData);
                    break;
                case "SCREEN_SHARE_START":
                    handleScreenShareStart(userId, consultationId, messageData);
                    break;
                case "SCREEN_SHARE_STOP":
                    handleScreenShareStop(userId, consultationId, messageData);
                    break;
                case "CONNECTION_QUALITY_UPDATE":
                    handleConnectionQualityUpdate(userId, consultationId, messageData);
                    break;
                case "PARTICIPANT_READY":
                    handleParticipantReady(userId, consultationId, messageData);
                    break;
                case "PING":
                    handlePing(session);
                    break;
                case "CHAT_MESSAGE":
                    handleChatMessage(userId, consultationId, messageData);
                    break;
                default:
                    log.warn("Unknown WebRTC message type: {}", messageType);
            }
        } catch (Exception e) {
            log.error("Error handling video consultation WebSocket message", e);
            sendErrorMessage(session, "Error processing WebRTC message");
        }
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        log.error("Video consultation WebSocket transport error: {}", session.getId(), exception);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus closeStatus) throws Exception {
        String userId = extractUserIdFromSession(session);
        String consultationId = userConsultationMapping.get(userId);
        
        if (userId != null) {
            activeSessions.remove(userId);
            userConsultationMapping.remove(userId);
            
            if (consultationId != null) {
                // Remove from consultation participants
                Map<String, WebSocketSession> participants = consultationParticipants.get(consultationId);
                if (participants != null) {
                    participants.remove(userId);
                    if (participants.isEmpty()) {
                        consultationParticipants.remove(consultationId);
                    }
                }
                
                // Record participant leave in Redis
                cacheService.recordParticipantLeave(consultationId, Long.parseLong(userId),
                    extractUserType(userId, consultationId));

                // Notify other participants that user left
                broadcastToConsultation(consultationId, Map.of(
                    "type", "PARTICIPANT_LEFT",
                    "userId", userId,
                    "consultationId", consultationId,
                    "timestamp", System.currentTimeMillis()
                ), null);
            }
        }

        log.info("Video consultation WebSocket connection closed: {} with status: {}", session.getId(), closeStatus);
    }

    @Override
    public boolean supportsPartialMessages() {
        return false;
    }

    // Handle WebRTC offer signaling
    private void handleWebRTCOffer(String senderId, String consultationId, Map<String, Object> messageData) {
        String targetUserId = (String) messageData.get("targetUserId");
        Object offer = messageData.get("offer");
        
        Map<String, Object> offerMessage = Map.of(
            "type", "WEBRTC_OFFER",
            "senderId", senderId,
            "offer", offer,
            "timestamp", System.currentTimeMillis()
        );
        
        sendMessageToUser(targetUserId, offerMessage);
        log.debug("WebRTC offer sent from {} to {} in consultation {}", senderId, targetUserId, consultationId);
    }

    // Handle WebRTC answer signaling
    private void handleWebRTCAnswer(String senderId, String consultationId, Map<String, Object> messageData) {
        String targetUserId = (String) messageData.get("targetUserId");
        Object answer = messageData.get("answer");
        
        Map<String, Object> answerMessage = Map.of(
            "type", "WEBRTC_ANSWER",
            "senderId", senderId,
            "answer", answer,
            "timestamp", System.currentTimeMillis()
        );
        
        sendMessageToUser(targetUserId, answerMessage);
        log.debug("WebRTC answer sent from {} to {} in consultation {}", senderId, targetUserId, consultationId);
    }

    // Handle ICE candidate exchange
    private void handleICECandidate(String senderId, String consultationId, Map<String, Object> messageData) {
        String targetUserId = (String) messageData.get("targetUserId");
        Object candidate = messageData.get("candidate");
        
        Map<String, Object> candidateMessage = Map.of(
            "type", "ICE_CANDIDATE",
            "senderId", senderId,
            "candidate", candidate,
            "timestamp", System.currentTimeMillis()
        );
        
        sendMessageToUser(targetUserId, candidateMessage);
        log.debug("ICE candidate sent from {} to {} in consultation {}", senderId, targetUserId, consultationId);
    }

    // Handle media state changes (mute/unmute video/audio)
    private void handleMediaStateChange(String userId, String consultationId, Map<String, Object> messageData) {
        Boolean videoEnabled = (Boolean) messageData.get("videoEnabled");
        Boolean audioEnabled = (Boolean) messageData.get("audioEnabled");

        boolean video = videoEnabled != null ? videoEnabled : true;
        boolean audio = audioEnabled != null ? audioEnabled : true;

        // Record media state change in Redis
        cacheService.recordMediaStateChange(consultationId, Long.parseLong(userId),
            extractUserType(userId, consultationId), video, audio);

        Map<String, Object> stateMessage = Map.of(
            "type", "MEDIA_STATE_CHANGED",
            "userId", userId,
            "videoEnabled", video,
            "audioEnabled", audio,
            "timestamp", System.currentTimeMillis()
        );

        broadcastToConsultation(consultationId, stateMessage, userId);
        log.debug("Media state changed for user {} in consultation {}: video={}, audio={}",
                 userId, consultationId, video, audio);
    }

    // Handle screen sharing start
    private void handleScreenShareStart(String userId, String consultationId, Map<String, Object> messageData) {
        // Record screen share in Redis
        cacheService.recordScreenShare(consultationId, Long.parseLong(userId),
            extractUserType(userId, consultationId), "STARTED");

        Map<String, Object> shareMessage = Map.of(
            "type", "SCREEN_SHARE_STARTED",
            "userId", userId,
            "consultationId", consultationId,
            "timestamp", System.currentTimeMillis()
        );

        broadcastToConsultation(consultationId, shareMessage, userId);
        log.info("Screen sharing started by user {} in consultation {}", userId, consultationId);
    }

    // Handle screen sharing stop
    private void handleScreenShareStop(String userId, String consultationId, Map<String, Object> messageData) {
        // Record screen share in Redis
        cacheService.recordScreenShare(consultationId, Long.parseLong(userId),
            extractUserType(userId, consultationId), "STOPPED");

        Map<String, Object> shareMessage = Map.of(
            "type", "SCREEN_SHARE_STOPPED",
            "userId", userId,
            "consultationId", consultationId,
            "timestamp", System.currentTimeMillis()
        );

        broadcastToConsultation(consultationId, shareMessage, userId);
        log.info("Screen sharing stopped by user {} in consultation {}", userId, consultationId);
    }

    // Handle connection quality updates
    private void handleConnectionQualityUpdate(String userId, String consultationId, Map<String, Object> messageData) {
        Map<String, Object> qualityData = (Map<String, Object>) messageData.get("qualityData");
        
        Map<String, Object> qualityMessage = Map.of(
            "type", "CONNECTION_QUALITY_UPDATED",
            "userId", userId,
            "qualityData", qualityData,
            "timestamp", System.currentTimeMillis()
        );
        
        // Send to all participants for monitoring
        broadcastToConsultation(consultationId, qualityMessage, null);
        log.debug("Connection quality updated for user {} in consultation {}", userId, consultationId);
    }

    // Handle participant ready signal
    private void handleParticipantReady(String userId, String consultationId, Map<String, Object> messageData) {
        Map<String, Object> readyMessage = Map.of(
            "type", "PARTICIPANT_READY",
            "userId", userId,
            "consultationId", consultationId,
            "timestamp", System.currentTimeMillis()
        );
        
        broadcastToConsultation(consultationId, readyMessage, userId);
        log.info("Participant {} is ready in consultation {}", userId, consultationId);
    }

    // Handle ping messages for connection keepalive
    private void handlePing(WebSocketSession session) {
        try {
            Map<String, Object> pongMessage = Map.of(
                "type", "PONG",
                "timestamp", System.currentTimeMillis()
            );
            sendMessage(session, pongMessage);
        } catch (Exception e) {
            log.error("Error handling ping", e);
        }
    }

    // Handle chat messages
    private void handleChatMessage(String senderId, String consultationId, Map<String, Object> messageData) {
        String messageText = (String) messageData.get("message");
        Object timestamp = messageData.get("timestamp");

        // Record chat message in Redis
        cacheService.recordChatMessage(consultationId, Long.parseLong(senderId),
            extractUserType(senderId, consultationId), messageText, "TEXT");

        Map<String, Object> chatMessage = Map.of(
            "type", "CHAT_MESSAGE",
            "senderId", senderId,
            "message", messageText,
            "timestamp", timestamp != null ? timestamp : System.currentTimeMillis()
        );

        // Broadcast to all participants in consultation (excluding sender)
        broadcastToConsultation(consultationId, chatMessage, senderId);
        log.debug("Chat message from user {} in consultation {}: {}", senderId, consultationId, messageText);
    }

    // Send participants list to a session
    private void sendParticipantsList(WebSocketSession session, String consultationId) {
        Map<String, WebSocketSession> participants = consultationParticipants.get(consultationId);
        if (participants != null) {
            Map<String, Object> participantsMessage = Map.of(
                "type", "PARTICIPANTS_LIST",
                "consultationId", consultationId,
                "participants", participants.keySet(),
                "timestamp", System.currentTimeMillis()
            );
            sendMessage(session, participantsMessage);
        }
    }

    // Broadcast message to all participants in a consultation
    private void broadcastToConsultation(String consultationId, Map<String, Object> message, String excludeUserId) {
        Map<String, WebSocketSession> participants = consultationParticipants.get(consultationId);
        if (participants != null) {
            participants.entrySet().stream()
                .filter(entry -> excludeUserId == null || !excludeUserId.equals(entry.getKey()))
                .forEach(entry -> sendMessage(entry.getValue(), message));
        }
    }

    // Send message to specific user
    private void sendMessageToUser(String userId, Map<String, Object> message) {
        WebSocketSession session = activeSessions.get(userId);
        if (session != null && session.isOpen()) {
            sendMessage(session, message);
        } else {
            log.warn("Cannot send message to user {}: session not found or closed", userId);
        }
    }

    // Send message to WebSocket session
    private void sendMessage(WebSocketSession session, Map<String, Object> message) {
        try {
            if (session.isOpen()) {
                String jsonMessage = objectMapper.writeValueAsString(message);
                session.sendMessage(new TextMessage(jsonMessage));
            }
        } catch (IOException e) {
            log.error("Error sending video consultation WebSocket message", e);
        }
    }

    // Send error message
    private void sendErrorMessage(WebSocketSession session, String errorMessage) {
        Map<String, Object> error = Map.of(
            "type", "ERROR",
            "message", errorMessage,
            "timestamp", System.currentTimeMillis()
        );
        sendMessage(session, error);
    }

    // Extract user ID from session (implement based on your authentication)
    private String extractUserIdFromSession(WebSocketSession session) {
        String query = session.getUri().getQuery();
        if (query != null && query.contains("userId=")) {
            return query.split("userId=")[1].split("&")[0];
        }
        return null;
    }

    // Extract consultation ID from session
    private String extractConsultationIdFromSession(WebSocketSession session) {
        String query = session.getUri().getQuery();
        if (query != null && query.contains("consultationId=")) {
            return query.split("consultationId=")[1].split("&")[0];
        }
        return null;
    }

    // Public methods for external services to send messages
    public void sendWebRTCSignal(String consultationId, String senderId, String targetUserId, String signalType, Object signalData) {
        Map<String, Object> signal = Map.of(
            "type", signalType,
            "senderId", senderId,
            "targetUserId", targetUserId,
            "data", signalData,
            "timestamp", System.currentTimeMillis()
        );
        
        sendMessageToUser(targetUserId, signal);
    }

    public void notifyConsultationUpdate(String consultationId, Map<String, Object> updateData) {
        Map<String, Object> notification = Map.of(
            "type", "CONSULTATION_UPDATED",
            "consultationId", consultationId,
            "updateData", updateData,
            "timestamp", System.currentTimeMillis()
        );
        
        broadcastToConsultation(consultationId, notification, null);
    }

    // Get active participants count for a consultation
    public int getActiveParticipantsCount(String consultationId) {
        Map<String, WebSocketSession> participants = consultationParticipants.get(consultationId);
        return participants != null ? participants.size() : 0;
    }

    // Check if user is connected to consultation
    public boolean isUserConnected(String consultationId, String userId) {
        Map<String, WebSocketSession> participants = consultationParticipants.get(consultationId);
        return participants != null && participants.containsKey(userId);
    }

    // Helper method to extract user type from query parameters
    private String extractUserType(String userId, String consultationId) {
        // Try to get user type from session query parameters
        Map<String, WebSocketSession> participants = consultationParticipants.get(consultationId);
        if (participants != null) {
            WebSocketSession session = participants.get(userId);
            if (session != null) {
                String query = session.getUri().getQuery();
                if (query != null && query.contains("userType=")) {
                    return query.split("userType=")[1].split("&")[0];
                }
            }
        }
        // Default fallback
        return "UNKNOWN";
    }
}