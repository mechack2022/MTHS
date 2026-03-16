package com.mths.shared.config;

import com.mths.shared.websocket.ChatWebSocketHandler;
import com.mths.shared.websocket.VideoConsultationWebSocketHandler;
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
                .setAllowedOriginPatterns("*") // Allow all origins for development
                .withSockJS();

        // Video consultation signaling WebSocket endpoint
        registry.addHandler(videoConsultationWebSocketHandler, "/ws/video-consultation")
                .setAllowedOriginPatterns("*") // Allow all origins for development
                .withSockJS();
    }
}