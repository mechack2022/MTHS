package com.mths.consultation.service;

import com.mths.consultation.dto.ConnectionQualityDTO;
import com.mths.consultation.dto.CreateVideoConsultationRequest;
import com.mths.consultation.dto.JoinConsultationResponse;
import com.mths.consultation.dto.VideoConsultationDTO;
import com.mths.consultation.entity.Appointment;
import com.mths.consultation.entity.VideoConsultation;
import com.mths.consultation.repository.AppointmentRepository;
import com.mths.consultation.repository.VideoConsultationRepository;
import com.mths.hospital.entity.DoctorProfile;
import com.mths.hospital.repository.DoctorProfileRepository;
import com.mths.patient.entity.PatientProfile;
import com.mths.patient.repository.PatientProfileRepository;
import com.mths.shared.constants.ConsultationStatus;
import com.mths.auth.dto.*;
import com.mths.auth.entity.*;
import com.mths.shared.exceptions.ResourceNotFoundException;
import com.mths.shared.mapper.VideoConsultationMapper;
import com.mths.auth.repository.*;
import com.mths.shared.websocket.VideoConsultationWebSocketHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class VideoConsultationServiceImpl implements VideoConsultationService {

    private final VideoConsultationRepository videoConsultationRepository;
    private final AppointmentRepository appointmentRepository;
    private final UserRepository userRepository;
    private final PatientProfileRepository patientProfileRepository;
    private final DoctorProfileRepository doctorProfileRepository;
    private final VideoConsultationMapper videoConsultationMapper;
    private final VideoConsultationWebSocketHandler webSocketHandler;

    @Override
    public VideoConsultationDTO createVideoConsultation(CreateVideoConsultationRequest request) {
        log.info("Creating video consultation for appointment: {}", request.getAppointmentId());
        
        Appointment appointment = appointmentRepository.findById(request.getAppointmentId())
            .orElseThrow(() -> new ResourceNotFoundException("Appointment", "id", request.getAppointmentId()));

        // Check if consultation already exists for this appointment
        if (videoConsultationRepository.existsByAppointment(appointment)) {
            throw new RuntimeException("Video consultation already exists for this appointment");
        }

        VideoConsultation consultation = new VideoConsultation();
        consultation.setAppointment(appointment);
        consultation.setSessionId(generateSessionId());
        consultation.setRoomId(generateRoomId());
        consultation.setStatus(ConsultationStatus.SCHEDULED);
        consultation.setStartTime(request.getScheduledStartTime());
        consultation.setDurationMinutes(request.getEstimatedDuration());
        consultation.setRecordingEnabled(request.getRecordingEnabled() != null ? request.getRecordingEnabled() : false);

        VideoConsultation saved = videoConsultationRepository.save(consultation);
        log.info("Video consultation created with session ID: {}", saved.getSessionId());
        
        return videoConsultationMapper.toDTO(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public VideoConsultationDTO getVideoConsultation(Long id) {
        VideoConsultation consultation = videoConsultationRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("VideoConsultation", "id", id));
        return videoConsultationMapper.toDTO(consultation);
    }

    @Override
    public VideoConsultationDTO updateVideoConsultationStatus(Long id, String status) {
        VideoConsultation consultation = videoConsultationRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("VideoConsultation", "id", id));

        ConsultationStatus previousStatus = consultation.getStatus();
        ConsultationStatus newStatus = ConsultationStatus.valueOf(status.toUpperCase());
        consultation.setStatus(newStatus);

        if (ConsultationStatus.IN_PROGRESS.equals(newStatus) && !ConsultationStatus.IN_PROGRESS.equals(previousStatus)) {
            // Set actual start time when consultation becomes active
            // VideoConsultation entity doesn't have actualStartTime, using startTime
            if (consultation.getStartTime() == null) {
                consultation.setStartTime(LocalDateTime.now());
            }
        } else if (ConsultationStatus.COMPLETED.equals(newStatus)) {
            consultation.setEndTime(LocalDateTime.now());
            if (consultation.getStartTime() != null) {
                long duration = java.time.Duration.between(
                    consultation.getStartTime(), 
                    consultation.getEndTime()
                ).toMinutes();
                consultation.setDurationMinutes((int) duration);
            }
        }

        VideoConsultation saved = videoConsultationRepository.save(consultation);
        
        // Notify participants about status change
        webSocketHandler.notifyConsultationUpdate(
            saved.getSessionId(),
            Map.of("status", status, "timestamp", System.currentTimeMillis())
        );

        log.info("Video consultation {} status updated from {} to {}", id, previousStatus, status);
        return videoConsultationMapper.toDTO(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<VideoConsultationDTO> getConsultationsByAppointment(Long appointmentId) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
            .orElseThrow(() -> new ResourceNotFoundException("Appointment", "id", appointmentId));
            
        return videoConsultationRepository.findByAppointment(appointment)
            .stream()
            .map(videoConsultationMapper::toDTO)
            .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<VideoConsultationDTO> getActiveConsultationsByUser(Long userId, String userType) {
        List<VideoConsultation> consultations;
        
        if ("PATIENT".equals(userType)) {
            PatientProfile profile = patientProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("PatientProfile", "userId", userId));
            consultations = videoConsultationRepository.findActiveConsultationsByPatient(profile);
        } else if ("DOCTOR".equals(userType)) {
            DoctorProfile profile = doctorProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("DoctorProfile", "userId", userId));
            consultations = videoConsultationRepository.findActiveConsultationsByDoctor(profile);
        } else {
            throw new IllegalArgumentException("Invalid user type: " + userType);
        }
        
        return consultations.stream()
            .map(videoConsultationMapper::toDTO)
            .collect(Collectors.toList());
    }

    @Override
    public JoinConsultationResponse joinConsultation(Long consultationId, Long userId, String userType) {
        VideoConsultation consultation = videoConsultationRepository.findById(consultationId)
            .orElseThrow(() -> new ResourceNotFoundException("VideoConsultation", "id", consultationId));

        // Validate user can join this consultation
        validateUserCanJoinConsultation(consultation, userId, userType);

        // Update consultation status if needed
        if (ConsultationStatus.SCHEDULED.equals(consultation.getStatus())) {
            updateVideoConsultationStatus(consultationId, "IN_PROGRESS");
            consultation = videoConsultationRepository.findById(consultationId).get();
        }

        // Prepare join response
        JoinConsultationResponse response = new JoinConsultationResponse();
        response.setSessionId(consultation.getSessionId());
        response.setRoomId(consultation.getRoomId());
        response.setToken(generateAccessToken(consultation, userId, userType));
        response.setIceServers(getIceServersConfig());
        response.setMediaConstraints(getMediaConstraints());
        response.setRecordingEnabled(consultation.getRecordingEnabled());
        response.setConsultationDetails(videoConsultationMapper.toDTO(consultation));
        
        // Get current participants
        response.setParticipants(getCurrentParticipants(consultation));

        log.info("User {} ({}) joined video consultation {}", userId, userType, consultationId);
        return response;
    }

    @Override
    public void leaveConsultation(Long consultationId, Long userId) {
        log.info("User {} leaving video consultation {}", userId, consultationId);
        
        // WebSocket handler will handle the actual disconnection
        // This method can be used for cleanup or logging purposes
    }

    @Override
    public void endConsultation(Long consultationId) {
        VideoConsultation consultation = videoConsultationRepository.findById(consultationId)
            .orElseThrow(() -> new ResourceNotFoundException("VideoConsultation", "id", consultationId));

        updateVideoConsultationStatus(consultationId, "COMPLETED");
        
        // Notify all participants that consultation has ended
        webSocketHandler.notifyConsultationUpdate(
            consultation.getSessionId(),
            Map.of("type", "CONSULTATION_ENDED", "timestamp", System.currentTimeMillis())
        );

        log.info("Video consultation {} ended", consultationId);
    }

    @Override
    public ConnectionQualityDTO updateConnectionQuality(Long consultationId, ConnectionQualityDTO qualityData) {
        VideoConsultation consultation = videoConsultationRepository.findById(consultationId)
            .orElseThrow(() -> new ResourceNotFoundException("VideoConsultation", "id", consultationId));

        qualityData.setConsultationId(consultationId);
        qualityData.setLastUpdated(LocalDateTime.now());
        
        // Here you could save quality metrics to a separate table if needed
        // For now, just broadcast to participants
        webSocketHandler.notifyConsultationUpdate(
            consultation.getSessionId(),
            Map.of("type", "CONNECTION_QUALITY_UPDATE", "qualityData", qualityData)
        );

        return qualityData;
    }

    @Override
    @Transactional(readOnly = true)
    public List<VideoConsultationDTO> getConsultationHistory(Long userId, String userType) {
        List<VideoConsultation> consultations;
        
        if ("PATIENT".equals(userType)) {
            PatientProfile profile = patientProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("PatientProfile", "userId", userId));
            consultations = videoConsultationRepository.findCompletedConsultationsByPatient(profile);
        } else if ("DOCTOR".equals(userType)) {
            DoctorProfile profile = doctorProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("DoctorProfile", "userId", userId));
            consultations = videoConsultationRepository.findCompletedConsultationsByDoctor(profile);
        } else {
            throw new IllegalArgumentException("Invalid user type: " + userType);
        }
        
        return consultations.stream()
            .map(videoConsultationMapper::toDTO)
            .collect(Collectors.toList());
    }

    @Override
    public void recordConsultationEvent(Long consultationId, String eventType, String eventData) {
        log.info("Recording consultation event - Consultation: {}, Event: {}, Data: {}", 
                consultationId, eventType, eventData);
        
        // Here you could save events to a consultation events table
        // For now, just log the event
    }

    private void validateUserCanJoinConsultation(VideoConsultation consultation, Long userId, String userType) {
        Appointment appointment = consultation.getAppointment();
        
        if ("PATIENT".equals(userType)) {
            if (!appointment.getPatientProfile().getUser().getId().equals(userId)) {
                throw new IllegalArgumentException("User is not the patient for this consultation");
            }
        } else if ("DOCTOR".equals(userType)) {
            if (!appointment.getDoctorProfile().getUser().getId().equals(userId)) {
                throw new IllegalArgumentException("User is not the doctor for this consultation");
            }
        } else {
            throw new IllegalArgumentException("Invalid user type: " + userType);
        }
    }

    private String generateSessionId() {
        return "session_" + UUID.randomUUID().toString().replace("-", "");
    }

    private String generateRoomId() {
        return "room_" + UUID.randomUUID().toString().replace("-", "");
    }

    private String generateAccessToken(VideoConsultation consultation, Long userId, String userType) {
        // In a real implementation, this would generate a JWT token for WebRTC access
        return "token_" + consultation.getSessionId() + "_" + userId + "_" + userType;
    }

    private Map<String, Object> getIceServersConfig() {
        // Configure STUN/TURN servers for WebRTC
        return Map.of(
            "iceServers", List.of(
                Map.of("urls", "stun:stun.l.google.com:19302"),
                Map.of("urls", "stun:stun1.l.google.com:19302")
            )
        );
    }

    private Map<String, Object> getMediaConstraints() {
        return Map.of(
            "video", Map.of(
                "width", Map.of("min", 640, "ideal", 1280, "max", 1920),
                "height", Map.of("min", 480, "ideal", 720, "max", 1080),
                "frameRate", Map.of("min", 15, "ideal", 30, "max", 30)
            ),
            "audio", Map.of(
                "echoCancellation", true,
                "noiseSuppression", true,
                "autoGainControl", true
            )
        );
    }

    private List<JoinConsultationResponse.ParticipantDTO> getCurrentParticipants(VideoConsultation consultation) {
        Appointment appointment = consultation.getAppointment();
        
        return List.of(
            createParticipantDTO(
                appointment.getPatientProfile().getUser(),
                "PATIENT",
                webSocketHandler.isUserConnected(consultation.getSessionId(), 
                    appointment.getPatientProfile().getUser().getId().toString())
            ),
            createParticipantDTO(
                appointment.getDoctorProfile().getUser(),
                "DOCTOR", 
                webSocketHandler.isUserConnected(consultation.getSessionId(),
                    appointment.getDoctorProfile().getUser().getId().toString())
            )
        );
    }

    private JoinConsultationResponse.ParticipantDTO createParticipantDTO(User user, String userType, boolean isOnline) {
        JoinConsultationResponse.ParticipantDTO participant = new JoinConsultationResponse.ParticipantDTO();
        participant.setUserId(user.getId());
        participant.setUserType(userType);
        participant.setName(user.getFirstName() + " " + user.getLastName());
        
        // Get profile image URL from the user's primary profile
        UserProfile primaryProfile = user.getPrimaryProfile();
        participant.setProfileImageUrl(primaryProfile != null ? primaryProfile.getProfileImageUrl() : null);
        
        participant.setIsOnline(isOnline);
        participant.setConnectionStatus(isOnline ? "CONNECTED" : "DISCONNECTED");
        return participant;
    }
}