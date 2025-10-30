package com.mths.consultation.entity;

import com.mths.shared.constants.ConsultationStatus;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "video_consultations")
@Data
@EqualsAndHashCode(callSuper = false)
public class VideoConsultation extends com.mths.shared.entity.BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "appointment_id", nullable = false)
    private Appointment appointment;

    @Column(name = "session_id", unique = true, nullable = false)
    private String sessionId;

    @Column(name = "room_id", unique = true, nullable = false)
    private String roomId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private ConsultationStatus status = ConsultationStatus.SCHEDULED;

    @Column(name = "start_time")
    private LocalDateTime startTime;

    @Column(name = "end_time")
    private LocalDateTime endTime;

    @Column(name = "duration_minutes")
    private Integer durationMinutes;

    @Column(name = "patient_joined_at")
    private LocalDateTime patientJoinedAt;

    @Column(name = "doctor_joined_at")
    private LocalDateTime doctorJoinedAt;

    @Column(name = "patient_left_at")
    private LocalDateTime patientLeftAt;

    @Column(name = "doctor_left_at")
    private LocalDateTime doctorLeftAt;

    @Column(name = "recording_url")
    private String recordingUrl;

    @Column(name = "recording_enabled")
    private Boolean recordingEnabled = false;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @Column(name = "connection_quality")
    private String connectionQuality;

    @Column(name = "technical_issues", columnDefinition = "TEXT")
    private String technicalIssues;

    // Chat messages during consultation
    @OneToMany(mappedBy = "videoConsultation", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<ChatMessage> chatMessages = new ArrayList<>();

    // File attachments shared during consultation
    @OneToMany(mappedBy = "videoConsultation", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<FileAttachment> fileAttachments = new ArrayList<>();

    // Helper methods
    public boolean isActive() {
        return status == ConsultationStatus.IN_PROGRESS;
    }

    public boolean canJoin() {
        return status == ConsultationStatus.SCHEDULED || status == ConsultationStatus.WAITING;
    }

    public Long getAppointmentId() {
        return appointment != null ? appointment.getId() : null;
    }

    public Long getPatientProfileId() {
        return appointment != null && appointment.getPatientProfile() != null 
            ? appointment.getPatientProfile().getId() : null;
    }

    public Long getDoctorProfileId() {
        return appointment != null && appointment.getDoctorProfile() != null 
            ? appointment.getDoctorProfile().getId() : null;
    }
}