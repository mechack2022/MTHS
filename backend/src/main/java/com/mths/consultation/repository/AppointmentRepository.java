package com.mths.consultation.repository;

import com.mths.shared.constants.AppointmentStatus;
import com.mths.consultation.entity.Appointment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, Long> {
    
    // Find by patient
    List<Appointment> findByPatientProfile_IdOrderByScheduledDatetimeDesc(Long patientProfileId);
    
    Page<Appointment> findByPatientProfile_IdOrderByScheduledDatetimeDesc(Long patientProfileId, Pageable pageable);
    
    // Find by doctor
    List<Appointment> findByDoctorProfile_IdOrderByScheduledDatetimeDesc(Long doctorProfileId);
    
    Page<Appointment> findByDoctorProfile_IdOrderByScheduledDatetimeDesc(Long doctorProfileId, Pageable pageable);
    
    // Find by status
    List<Appointment> findByStatus(AppointmentStatus status);
    
    // Find by patient and status
    List<Appointment> findByPatientProfile_IdAndStatus(Long patientProfileId, AppointmentStatus status);
    
    // Find by doctor and status
    List<Appointment> findByDoctorProfile_IdAndStatus(Long doctorProfileId, AppointmentStatus status);
    
    // Find appointments by date range
    @Query("SELECT a FROM Appointment a WHERE a.scheduledDatetime BETWEEN :startDate AND :endDate ORDER BY a.scheduledDatetime ASC")
    List<Appointment> findAppointmentsByDateRange(@Param("startDate") LocalDateTime startDate, 
                                                @Param("endDate") LocalDateTime endDate);
    
    // Find doctor's appointments for a specific date
    @Query("SELECT a FROM Appointment a WHERE a.doctorProfile.id = :doctorProfileId " +
           "AND DATE(a.scheduledDatetime) = DATE(:date) " +
           "AND a.status IN ('SCHEDULED', 'CONFIRMED', 'IN_PROGRESS') " +
           "ORDER BY a.scheduledDatetime ASC")
    List<Appointment> findDoctorAppointmentsForDate(@Param("doctorProfileId") Long doctorProfileId, 
                                                   @Param("date") LocalDateTime date);
    
    // Find patient's upcoming appointments
    @Query("SELECT a FROM Appointment a WHERE a.patientProfile.id = :patientProfileId " +
           "AND a.scheduledDatetime > :currentDate " +
           "AND a.status IN ('SCHEDULED', 'CONFIRMED') " +
           "ORDER BY a.scheduledDatetime ASC")
    List<Appointment> findPatientUpcomingAppointments(@Param("patientProfileId") Long patientProfileId, 
                                                     @Param("currentDate") LocalDateTime currentDate);
    
    // Find doctor's upcoming appointments
    @Query("SELECT a FROM Appointment a WHERE a.doctorProfile.id = :doctorProfileId " +
           "AND a.scheduledDatetime > :currentDate " +
           "AND a.status IN ('SCHEDULED', 'CONFIRMED') " +
           "ORDER BY a.scheduledDatetime ASC")
    List<Appointment> findDoctorUpcomingAppointments(@Param("doctorProfileId") Long doctorProfileId, 
                                                    @Param("currentDate") LocalDateTime currentDate);
    
    // Check for conflicting appointments
    @Query("SELECT a FROM Appointment a WHERE a.doctorProfile.id = :doctorProfileId " +
           "AND a.scheduledDatetime BETWEEN :startTime AND :endTime " +
           "AND a.status IN ('SCHEDULED', 'CONFIRMED', 'IN_PROGRESS')")
    List<Appointment> findConflictingAppointments(@Param("doctorProfileId") Long doctorProfileId,
                                                 @Param("startTime") LocalDateTime startTime,
                                                 @Param("endTime") LocalDateTime endTime);
    
    // Find appointments requiring attention (overdue, etc.)
    @Query("SELECT a FROM Appointment a WHERE a.status = 'SCHEDULED' " +
           "AND a.scheduledDatetime < :currentTime")
    List<Appointment> findOverdueAppointments(@Param("currentTime") LocalDateTime currentTime);
    
    // Count appointments by status for dashboard
    @Query("SELECT COUNT(a) FROM Appointment a WHERE a.status = :status")
    Long countByStatus(@Param("status") AppointmentStatus status);
    
    // Count patient's appointments
    Long countByPatientProfile_Id(Long patientProfileId);
    
    // Count doctor's appointments
    Long countByDoctorProfile_Id(Long doctorProfileId);
    
    // Find appointments with vital signs having severe health status
    @Query("SELECT a FROM Appointment a WHERE a.id IN " +
           "(SELECT v.appointment.id FROM VitalSigns v WHERE v.healthStatus = 'SEVERE')")
    List<Appointment> findAppointmentsWithSevereVitals();
    
    // Monthly appointments report
    @Query("SELECT a FROM Appointment a WHERE YEAR(a.scheduledDatetime) = :year " +
           "AND MONTH(a.scheduledDatetime) = :month ORDER BY a.scheduledDatetime ASC")
    List<Appointment> findAppointmentsByMonth(@Param("year") int year, @Param("month") int month);
}