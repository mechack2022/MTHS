package com.mths.consultation.dto;

import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class JoinConsultationResponse {
    private String sessionId;
    private String roomId;
    private String token;
    private Map<String, Object> iceServers;
    private Map<String, Object> mediaConstraints;
    private Boolean recordingEnabled;
    private List<ParticipantDTO> participants;
    private VideoConsultationDTO consultationDetails;
    
    @Data
    public static class ParticipantDTO {
        private Long userId;
        private String userType;
        private String name;
        private String profileImageUrl;
        private Boolean isOnline;
        private String connectionStatus;
    }
}