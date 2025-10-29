package com.mths.consultation.repository;

import com.mths.consultation.entity.Appointment;
import com.mths.hospital.entity.DoctorProfile;
import com.mths.patient.entity.PatientProfile;
import com.mths.consultation.entity.VideoConsultation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface VideoConsultationRepository extends JpaRepository<VideoConsultation, Long> {
    
    Optional<VideoConsultation> findBySessionId(String sessionId);
    
    Optional<VideoConsultation> findByRoomId(String roomId);
    
    List<VideoConsultation> findByAppointment(Appointment appointment);
    
    boolean existsByAppointment(Appointment appointment);
    
    List<VideoConsultation> findByStatus(com.mths.shared.constants.ConsultationStatus status);
    
    @Query("SELECT vc FROM VideoConsultation vc WHERE vc.appointment.patientProfile = :patientProfile AND vc.status IN ('SCHEDULED', 'IN_PROGRESS')")
    List<VideoConsultation> findActiveConsultationsByPatient(@Param("patientProfile") PatientProfile patientProfile);
    
    @Query("SELECT vc FROM VideoConsultation vc WHERE vc.appointment.doctorProfile = :doctorProfile AND vc.status IN ('SCHEDULED', 'IN_PROGRESS')")
    List<VideoConsultation> findActiveConsultationsByDoctor(@Param("doctorProfile") DoctorProfile doctorProfile);
    
    @Query("SELECT vc FROM VideoConsultation vc WHERE vc.appointment.patientProfile = :patientProfile AND vc.status = 'COMPLETED' ORDER BY vc.endTime DESC")
    List<VideoConsultation> findCompletedConsultationsByPatient(@Param("patientProfile") PatientProfile patientProfile);
    
    @Query("SELECT vc FROM VideoConsultation vc WHERE vc.appointment.doctorProfile = :doctorProfile AND vc.status = 'COMPLETED' ORDER BY vc.endTime DESC")
    List<VideoConsultation> findCompletedConsultationsByDoctor(@Param("doctorProfile") DoctorProfile doctorProfile);
    
    @Query("SELECT vc FROM VideoConsultation vc WHERE vc.startTime BETWEEN :startDate AND :endDate")
    List<VideoConsultation> findByScheduledTimeBetween(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
    
    @Query("SELECT vc FROM VideoConsultation vc WHERE vc.appointment.patientProfile = :patientProfile ORDER BY vc.startTime DESC")
    List<VideoConsultation> findAllConsultationsByPatient(@Param("patientProfile") PatientProfile patientProfile);
    
    @Query("SELECT vc FROM VideoConsultation vc WHERE vc.appointment.doctorProfile = :doctorProfile ORDER BY vc.startTime DESC")
    List<VideoConsultation> findAllConsultationsByDoctor(@Param("doctorProfile") DoctorProfile doctorProfile);
    
    @Query("SELECT vc FROM VideoConsultation vc WHERE vc.recordingEnabled = true AND vc.status = 'COMPLETED'")
    List<VideoConsultation> findCompletedRecordedConsultations();
    
    @Query("SELECT COUNT(vc) FROM VideoConsultation vc WHERE vc.appointment.patientProfile = :patientProfile")
    long countConsultationsByPatient(@Param("patientProfile") PatientProfile patientProfile);
    
    @Query("SELECT COUNT(vc) FROM VideoConsultation vc WHERE vc.appointment.doctorProfile = :doctorProfile")
    long countConsultationsByDoctor(@Param("doctorProfile") DoctorProfile doctorProfile);
    
    @Query("SELECT vc FROM VideoConsultation vc WHERE vc.status = 'SCHEDULED' AND vc.startTime < :threshold")
    List<VideoConsultation> findOverdueConsultations(@Param("threshold") LocalDateTime threshold);
    
    @Query("SELECT vc FROM VideoConsultation vc WHERE vc.status = 'IN_PROGRESS' AND vc.startTime < :threshold")
    List<VideoConsultation> findLongRunningConsultations(@Param("threshold") LocalDateTime threshold);
}