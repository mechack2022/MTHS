package com.mths.consultation.repository;

import com.mths.consultation.entity.VideoConsultationSession;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Redis repository for managing video consultation session cache
 * Provides fast access to active consultation session data
 */
@Repository
public interface VideoConsultationSessionRepository extends CrudRepository<VideoConsultationSession, String> {

    Optional<VideoConsultationSession> findByConsultationId(Long consultationId);
    Optional<VideoConsultationSession> findByRoomId(String roomId);
    List<VideoConsultationSession> findByStatus(String status);
    List<VideoConsultationSession> findByPatientId(Long patientId);
    List<VideoConsultationSession> findByDoctorId(Long doctorId);
    List<VideoConsultationSession> findByFlushedToDatabase(boolean flushed);
}
