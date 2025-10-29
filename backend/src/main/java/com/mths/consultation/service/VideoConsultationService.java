package com.mths.consultation.service;

import com.mths.auth.dto.*;
import com.mths.consultation.dto.ConnectionQualityDTO;
import com.mths.consultation.dto.CreateVideoConsultationRequest;
import com.mths.consultation.dto.JoinConsultationResponse;
import com.mths.consultation.dto.VideoConsultationDTO;

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