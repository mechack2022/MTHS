package com.mths.consultation.controller;

import com.mths.consultation.dto.*;
import com.mths.shared.dto.ApiResponse;
import com.mths.consultation.service.VideoConsultationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/video-consultations")
@Slf4j
@RequiredArgsConstructor
public class VideoConsultationController {

    private final VideoConsultationService videoConsultationService;

    // ========================================================================
    // VIDEO CONSULTATION MANAGEMENT
    // ========================================================================

    @PostMapping("/appointments/{appointmentId}/start")
    @PreAuthorize("hasRole('DOCTOR') or hasRole('PATIENT')")
    public ResponseEntity<ApiResponse<VideoConsultationDTO>> startVideoConsultation(
            @PathVariable Long appointmentId,
            @Valid @RequestBody StartConsultationRequest request) {
        
        log.info("Starting video consultation for appointment: {}", appointmentId);

        // Create a CreateVideoConsultationRequest from the appointment ID and start request
        CreateVideoConsultationRequest createRequest = new CreateVideoConsultationRequest();
        createRequest.setAppointmentId(appointmentId);
        createRequest.setRecordingEnabled(request.recordingEnabled());
        createRequest.setScheduledStartTime(java.time.LocalDateTime.now()); // Start immediately
        createRequest.setEstimatedDuration(60); // Default 60 minutes

        VideoConsultationDTO consultation = videoConsultationService.createVideoConsultation(createRequest);

        ApiResponse<VideoConsultationDTO> response = ApiResponse.success(
                "Video consultation started successfully",
                consultation,
                HttpStatus.OK.value()
        );

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{consultationId}")
    @PreAuthorize("hasRole('DOCTOR') or hasRole('PATIENT') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<VideoConsultationDTO>> getVideoConsultation(
            @PathVariable Long consultationId) {
        
        VideoConsultationDTO consultation = videoConsultationService.getVideoConsultation(consultationId);

        ApiResponse<VideoConsultationDTO> response = ApiResponse.success(
                "Video consultation retrieved successfully",
                consultation,
                HttpStatus.OK.value()
        );

        return ResponseEntity.ok(response);
    }

    @PostMapping("/{consultationId}/join")
    @PreAuthorize("hasRole('DOCTOR') or hasRole('PATIENT') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<JoinConsultationResponse>> joinConsultation(
            @PathVariable Long consultationId,
            @Valid @RequestBody JoinConsultationRequest request) {
        
        log.info("User {} joining consultation: {}", request.getUserId(), consultationId);

        JoinConsultationResponse joinResponse = videoConsultationService.joinConsultation(
                consultationId, request.getUserId(), request.getUserType());

        ApiResponse<JoinConsultationResponse> response = ApiResponse.success(
                "Successfully joined consultation",
                joinResponse,
                HttpStatus.OK.value()
        );

        return ResponseEntity.ok(response);
    }

    @PostMapping("/{consultationId}/leave")
    @PreAuthorize("hasRole('DOCTOR') or hasRole('PATIENT') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<String>> leaveConsultation(
            @PathVariable Long consultationId,
            @Valid @RequestBody LeaveConsultationRequest request) {
        
        log.info("User {} leaving consultation: {}", request.getUserId(), consultationId);

        videoConsultationService.leaveConsultation(consultationId, request.getUserId());

        ApiResponse<String> response = ApiResponse.success(
                "Successfully left consultation"
        );

        return ResponseEntity.ok(response);
    }

    @PutMapping("/{consultationId}/end")
    @PreAuthorize("hasRole('DOCTOR') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<VideoConsultationDTO>> endConsultation(
            @PathVariable Long consultationId,
            @Valid @RequestBody EndConsultationRequest request) {
        
        log.info("Ending consultation: {}", consultationId);

        videoConsultationService.endConsultation(consultationId);
        
        // Get the updated consultation to return
        VideoConsultationDTO consultation = videoConsultationService.getVideoConsultation(consultationId);

        ApiResponse<VideoConsultationDTO> response = ApiResponse.success(
                "Consultation ended successfully",
                consultation,
                HttpStatus.OK.value()
        );

        return ResponseEntity.ok(response);
    }

    // ========================================================================
    // CONSULTATION STATUS MANAGEMENT
    // ========================================================================

    @PutMapping("/{consultationId}/status")
    @PreAuthorize("hasRole('DOCTOR') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<VideoConsultationDTO>> updateConsultationStatus(
            @PathVariable Long consultationId,
            @RequestBody UpdateConsultationStatusRequest request) {
        
        log.info("Updating consultation {} status to: {}", consultationId, request.status());

        VideoConsultationDTO consultation = videoConsultationService.updateVideoConsultationStatus(
                consultationId, request.status());

        ApiResponse<VideoConsultationDTO> response = ApiResponse.success(
                "Consultation status updated successfully",
                consultation,
                HttpStatus.OK.value()
        );

        return ResponseEntity.ok(response);
    }

    @GetMapping("/appointments/{appointmentId}")
    @PreAuthorize("hasRole('DOCTOR') or hasRole('PATIENT') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<VideoConsultationDTO>>> getConsultationsByAppointment(
            @PathVariable Long appointmentId) {
        
        log.info("Retrieving consultations for appointment: {}", appointmentId);

        List<VideoConsultationDTO> consultations = videoConsultationService.getConsultationsByAppointment(appointmentId);

        ApiResponse<List<VideoConsultationDTO>> response = ApiResponse.success(
                "Consultations retrieved successfully",
                consultations,
                HttpStatus.OK.value()
        );

        return ResponseEntity.ok(response);
    }

    // ========================================================================
    // WEBRTC SIGNALING (handled via WebSocket)
    // ========================================================================
    
    // Note: WebRTC signaling is handled through the WebSocket connection
    // These endpoints are commented out as they're not implemented in the service
    
    /*
    @PostMapping("/{consultationId}/webrtc/offer")
    @PreAuthorize("hasRole('DOCTOR') or hasRole('PATIENT')")
    public ResponseEntity<ApiResponse<String>> handleWebRTCOffer(
            @PathVariable Long consultationId,
            @RequestBody Map<String, Object> offerData) {
        
        log.info("Handling WebRTC offer for consultation: {}", consultationId);
        // WebRTC signaling is handled through WebSocket connection
        return ResponseEntity.ok(ApiResponse.success("Use WebSocket for WebRTC signaling"));
    }
    */

    // ========================================================================
    // CONSULTATION HISTORY
    // ========================================================================

    @GetMapping("/users/{userId}/active")
    @PreAuthorize("hasRole('PATIENT') or hasRole('DOCTOR') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<VideoConsultationDTO>>> getActiveConsultationsByUser(
            @PathVariable Long userId,
            @RequestParam String userType) {
        
        log.info("Retrieving active consultations for user: {} ({})", userId, userType);

        List<VideoConsultationDTO> consultations = videoConsultationService.getActiveConsultationsByUser(userId, userType);

        ApiResponse<List<VideoConsultationDTO>> response = ApiResponse.success(
                "Active consultations retrieved successfully",
                consultations,
                HttpStatus.OK.value()
        );

        return ResponseEntity.ok(response);
    }

    @GetMapping("/users/{userId}/history")
    @PreAuthorize("hasRole('PATIENT') or hasRole('DOCTOR') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<VideoConsultationDTO>>> getConsultationHistory(
            @PathVariable Long userId,
            @RequestParam String userType) {
        
        log.info("Retrieving consultation history for user: {} ({})", userId, userType);

        List<VideoConsultationDTO> consultations = videoConsultationService.getConsultationHistory(userId, userType);

        ApiResponse<List<VideoConsultationDTO>> response = ApiResponse.success(
                "Consultation history retrieved successfully",
                consultations,
                HttpStatus.OK.value()
        );

        return ResponseEntity.ok(response);
    }

    // ========================================================================
    // CONNECTION QUALITY MANAGEMENT
    // ========================================================================

    @PostMapping("/{consultationId}/connection-quality")
    @PreAuthorize("hasRole('DOCTOR') or hasRole('PATIENT')")
    public ResponseEntity<ApiResponse<ConnectionQualityDTO>> updateConnectionQuality(
            @PathVariable Long consultationId,
            @Valid @RequestBody ConnectionQualityDTO qualityData) {
        
        log.info("Updating connection quality for consultation: {}", consultationId);

        ConnectionQualityDTO updatedQuality = videoConsultationService.updateConnectionQuality(consultationId, qualityData);

        ApiResponse<ConnectionQualityDTO> response = ApiResponse.success(
                "Connection quality updated successfully",
                updatedQuality,
                HttpStatus.OK.value()
        );

        return ResponseEntity.ok(response);
    }

    // ========================================================================
    // CONSULTATION EVENTS & MONITORING
    // ========================================================================

    @PostMapping("/{consultationId}/events")
    @PreAuthorize("hasRole('DOCTOR') or hasRole('PATIENT') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<String>> recordConsultationEvent(
            @PathVariable Long consultationId,
            @RequestBody ConsultationEventRequest request) {
        
        log.info("Recording event '{}' for consultation: {}", request.eventType(), consultationId);

        videoConsultationService.recordConsultationEvent(consultationId, request.eventType(), request.eventData());

        ApiResponse<String> response = ApiResponse.success(
                "Consultation event recorded successfully"
        );

        return ResponseEntity.ok(response);
    }

    // ========================================================================
    // REQUEST/RESPONSE DTOs
    // ========================================================================

    public record StartConsultationRequest(
            Boolean recordingEnabled,
            Map<String, Object> consultationSettings
    ) {}

    public record JoinConsultationRequest(
            Long userId,
            String userType, // PATIENT, DOCTOR
            Map<String, Object> deviceInfo
    ) {
        public Long getUserId() { return userId; }
        public String getUserType() { return userType; }
    }

    public record LeaveConsultationRequest(
            Long userId,
            String userType,
            String reason
    ) {
        public Long getUserId() { return userId; }
    }

    public record EndConsultationRequest(
            String consultationNotes,
            String diagnosis,
            Integer rating
    ) {}

    public record UpdateConsultationStatusRequest(
            String status,
            String reason
    ) {}

    public record ConsultationEventRequest(
            String eventType,
            String eventData,
            Long userId
    ) {}
}