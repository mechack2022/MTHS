package com.auth.service.websocket;

import com.auth.service.controller.ChatController;
import com.auth.service.dto.ChatMessageDTO;
import com.auth.service.service.ChatMessageService;
import com.fasterxml.jackson.databind.ObjectMapper;
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
public class ChatWebSocketHandler implements WebSocketHandler {

    private final ChatMessageService chatMessageService;
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    // Store active sessions: userId -> WebSocketSession
    private final Map<String, WebSocketSession> activeSessions = new ConcurrentHashMap<>();
    
    // Store user consultation mapping: userId -> consultationId
    private final Map<String, String> userConsultationMapping = new ConcurrentHashMap<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        log.info("WebSocket connection established: {}", session.getId());
        
        // Extract user information from session attributes or query parameters
        String userId = extractUserIdFromSession(session);
        String consultationId = extractConsultationIdFromSession(session);
        
        if (userId != null && consultationId != null) {
            activeSessions.put(userId, session);
            userConsultationMapping.put(userId, consultationId);
            
            // Notify other participants that user joined
            broadcastUserPresence(consultationId, userId, "JOINED");
            
            log.info("User {} joined consultation {} via WebSocket", userId, consultationId);
        }
    }

    @Override
    public void handleMessage(WebSocketSession session, WebSocketMessage<?> message) throws Exception {
        try {
            String payload = (String) message.getPayload();
            Map<String, Object> messageData = objectMapper.readValue(payload, Map.class);
            
            String messageType = (String) messageData.get("type");
            String userId = extractUserIdFromSession(session);
            
            switch (messageType) {
                case "CHAT_MESSAGE":
                    handleChatMessage(userId, messageData);
                    break;
                case "MESSAGE_READ":
                    handleMessageRead(userId, messageData);
                    break;
                case "TYPING_START":
                    handleTypingIndicator(userId, messageData, true);
                    break;
                case "TYPING_STOP":
                    handleTypingIndicator(userId, messageData, false);
                    break;
                case "PING":
                    handlePing(session);
                    break;
                default:
                    log.warn("Unknown message type: {}", messageType);
            }
        } catch (Exception e) {
            log.error("Error handling WebSocket message", e);
            sendErrorMessage(session, "Error processing message");
        }
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        log.error("WebSocket transport error: {}", session.getId(), exception);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus closeStatus) throws Exception {
        String userId = extractUserIdFromSession(session);
        String consultationId = userConsultationMapping.get(userId);
        
        if (userId != null) {
            activeSessions.remove(userId);
            userConsultationMapping.remove(userId);
            
            if (consultationId != null) {
                // Notify other participants that user left
                broadcastUserPresence(consultationId, userId, "LEFT");
            }
        }
        
        log.info("WebSocket connection closed: {} with status: {}", session.getId(), closeStatus);
    }

    @Override
    public boolean supportsPartialMessages() {
        return false;
    }

    // Handle incoming chat messages
    private void handleChatMessage(String senderId, Map<String, Object> messageData) {
        try {
            String consultationId = (String) messageData.get("consultationId");
            String receiverId = (String) messageData.get("receiverId");
            String content = (String) messageData.get("content");
            String messageType = (String) messageData.get("messageType");
            String receiverType = (String) messageData.get("receiverType");
            String senderType = (String) messageData.get("senderType");
            Long replyToMessageId = messageData.get("replyToMessageId") != null ?
                Long.valueOf(messageData.get("replyToMessageId").toString()) : null;

            // Create request object
            ChatController.SendMessageRequest request = new ChatController.SendMessageRequest(
                Long.valueOf(senderId),
                senderType != null ? senderType : "PATIENT",
                Long.valueOf(receiverId),
                receiverType != null ? receiverType : "DOCTOR",
                content,
                messageType != null ? messageType : "TEXT",
                replyToMessageId
            );

            // Save message to database
            ChatMessageDTO savedMessage = chatMessageService.sendMessage(
                Long.valueOf(consultationId),
                request
            );

            Long messageId = savedMessage.getId();
            
            // Prepare message for broadcasting
            Map<String, Object> broadcastMessage = Map.of(
                "type", "NEW_MESSAGE",
                "messageId", messageId,
                "consultationId", consultationId,
                "senderId", senderId,
                "receiverId", receiverId,
                "content", content,
                "messageType", messageType,
                "timestamp", System.currentTimeMillis()
            );
            
            // Send to receiver
            sendMessageToUser(receiverId, broadcastMessage);
            
            // Send confirmation to sender
            Map<String, Object> confirmation = Map.of(
                "type", "MESSAGE_SENT",
                "messageId", messageId,
                "status", "DELIVERED"
            );
            sendMessageToUser(senderId, confirmation);
            
        } catch (Exception e) {
            log.error("Error handling chat message", e);
        }
    }

    // Handle message read receipts
    private void handleMessageRead(String userId, Map<String, Object> messageData) {
        try {
            Long messageId = Long.valueOf((String) messageData.get("messageId"));
            
            // Update message status in database
            ChatMessageDTO readMessage = chatMessageService.markMessageAsRead(messageId, Long.valueOf(userId));

            // Notify sender about read receipt
            String senderId = readMessage.getSenderId().toString();
            if (senderId != null) {
                Map<String, Object> readReceipt = Map.of(
                    "type", "MESSAGE_READ_RECEIPT",
                    "messageId", messageId,
                    "readBy", userId,
                    "timestamp", System.currentTimeMillis()
                );
                sendMessageToUser(senderId, readReceipt);
            }
            
        } catch (Exception e) {
            log.error("Error handling message read", e);
        }
    }

    // Handle typing indicators
    private void handleTypingIndicator(String userId, Map<String, Object> messageData, boolean isTyping) {
        try {
            String consultationId = (String) messageData.get("consultationId");
            String receiverId = (String) messageData.get("receiverId");
            
            Map<String, Object> typingMessage = Map.of(
                "type", isTyping ? "USER_TYPING" : "USER_STOPPED_TYPING",
                "userId", userId,
                "consultationId", consultationId,
                "timestamp", System.currentTimeMillis()
            );
            
            sendMessageToUser(receiverId, typingMessage);
            
        } catch (Exception e) {
            log.error("Error handling typing indicator", e);
        }
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

    // Broadcast user presence changes
    private void broadcastUserPresence(String consultationId, String userId, String status) {
        try {
            Map<String, Object> presenceMessage = Map.of(
                "type", "USER_PRESENCE",
                "userId", userId,
                "consultationId", consultationId,
                "status", status,
                "timestamp", System.currentTimeMillis()
            );
            
            // Broadcast to all users in the same consultation
            userConsultationMapping.entrySet().stream()
                .filter(entry -> consultationId.equals(entry.getValue()) && !userId.equals(entry.getKey()))
                .forEach(entry -> sendMessageToUser(entry.getKey(), presenceMessage));
                
        } catch (Exception e) {
            log.error("Error broadcasting user presence", e);
        }
    }

    // Send message to specific user
    private void sendMessageToUser(String userId, Map<String, Object> message) {
        WebSocketSession session = activeSessions.get(userId);
        if (session != null && session.isOpen()) {
            sendMessage(session, message);
        }
    }

    // Send message to WebSocket session
    private void sendMessage(WebSocketSession session, Map<String, Object> message) {
        try {
            String jsonMessage = objectMapper.writeValueAsString(message);
            session.sendMessage(new TextMessage(jsonMessage));
        } catch (IOException e) {
            log.error("Error sending WebSocket message", e);
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
        // This should extract user ID from JWT token or session attributes
        // For now, returning from query parameters
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

    // Public method to send messages from other services
    public void sendMessageToUserFromService(String userId, Map<String, Object> message) {
        sendMessageToUser(userId, message);
    }

    // Get active users count for a consultation
    public long getActiveUsersCount(String consultationId) {
        return userConsultationMapping.values().stream()
            .mapToLong(id -> consultationId.equals(id) ? 1 : 0)
            .sum();
    }
}