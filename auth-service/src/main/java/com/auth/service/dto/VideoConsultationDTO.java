package com.auth.service.dto;

import com.auth.service.constants.ConsultationStatus;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class VideoConsultationDTO {
    private Long id;
    private Long appointmentId;
    private String sessionId;
    private String roomId;
    private ConsultationStatus status;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Integer durationMinutes;
    private LocalDateTime patientJoinedAt;
    private LocalDateTime doctorJoinedAt;
    private LocalDateTime patientLeftAt;
    private LocalDateTime doctorLeftAt;
    private String recordingUrl;
    private Boolean recordingEnabled;
    private String notes;
    private String connectionQuality;
    private String technicalIssues;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // Appointment details
    private String patientName;
    private String doctorName;
    private String doctorSpecialization;
    
    // Chat and files count
    private Integer messageCount;
    private Integer fileCount;
    
    // Status indicators
    private Boolean isActive;
    private Boolean canJoin;
    private Boolean patientOnline;
    private Boolean doctorOnline;
    
    // WebRTC configuration
    private String iceServers;
    private String turnServerCredentials;
}