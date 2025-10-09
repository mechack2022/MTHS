package com.auth.service.service;

import com.auth.service.dto.*;

import java.util.List;

public interface VideoConsultationService {
    VideoConsultationDTO createVideoConsultation(CreateVideoConsultationRequest request);
    VideoConsultationDTO getVideoConsultation(Long id);
    VideoConsultationDTO updateVideoConsultationStatus(Long id, String status);
    List<VideoConsultationDTO> getConsultationsByAppointment(Long appointmentId);
    List<VideoConsultationDTO> getActiveConsultationsByUser(Long userId, String userType);
    JoinConsultationResponse joinConsultation(Long consultationId, Long userId, String userType);
    void leaveConsultation(Long consultationId, Long userId);
    void endConsultation(Long consultationId);
    ConnectionQualityDTO updateConnectionQuality(Long consultationId, ConnectionQualityDTO qualityData);
    List<VideoConsultationDTO> getConsultationHistory(Long userId, String userType);
    void recordConsultationEvent(Long consultationId, String eventType, String eventData);
}