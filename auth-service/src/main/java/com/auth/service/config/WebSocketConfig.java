package com.auth.service.config;

import com.auth.service.websocket.ChatWebSocketHandler;
import com.auth.service.websocket.VideoConsultationWebSocketHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketConfigurer {

    private final ChatWebSocketHandler chatWebSocketHandler;
    private final VideoConsultationWebSocketHandler videoConsultationWebSocketHandler;

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        // Chat WebSocket endpoint
        registry.addHandler(chatWebSocketHandler, "/ws/chat")
                .setAllowedOrigins("*") // Configure based on your frontend origins
                .withSockJS();

        // Video consultation signaling WebSocket endpoint
        registry.addHandler(videoConsultationWebSocketHandler, "/ws/video-consultation")
                .setAllowedOrigins("*") // Configure based on your frontend origins
                .withSockJS();
    }
}