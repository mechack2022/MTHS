package com.mths.consultation.service;

import com.mths.consultation.dto.*;
import com.mths.patient.dto.VitalSignsDTO;
import com.mths.shared.constants.AppointmentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;

public interface AppointmentService {
    
    // Appointment Management
    AppointmentDTO createAppointment(CreateAppointmentRequest request);
    AppointmentDTO getAppointmentById(Long appointmentId);
    AppointmentDTO updateAppointment(Long appointmentId, UpdateAppointmentRequest request);
    void deleteAppointment(Long appointmentId);
    
    // Appointment Scheduling
    AppointmentDTO rescheduleAppointment(RescheduleAppointmentRequest request);
    AppointmentDTO cancelAppointment(CancelAppointmentRequest request);
    AppointmentDTO confirmAppointment(Long appointmentId);
    AppointmentDTO startConsultation(Long appointmentId);
    AppointmentDTO completeAppointment(Long appointmentId, String consultationNotes, String diagnosis);
    
    // Appointment Queries
    List<AppointmentDTO> getPatientAppointments(Long patientProfileId);
    List<AppointmentDTO> getDoctorAppointments(Long doctorProfileId);
    Page<AppointmentDTO> getPatientAppointments(Long patientProfileId, Pageable pageable);
    Page<AppointmentDTO> getDoctorAppointments(Long doctorProfileId, Pageable pageable);
    
    List<AppointmentDTO> getAppointmentsByStatus(AppointmentStatus status);
    List<AppointmentDTO> getPatientAppointmentsByStatus(Long patientProfileId, AppointmentStatus status);
    List<AppointmentDTO> getDoctorAppointmentsByStatus(Long doctorProfileId, AppointmentStatus status);
    
    // Date-based Queries
    List<AppointmentDTO> getAppointmentsByDateRange(LocalDateTime startDate, LocalDateTime endDate);
    List<AppointmentDTO> getDoctorAppointmentsForDate(Long doctorProfileId, LocalDateTime date);
    List<AppointmentDTO> getPatientUpcomingAppointments(Long patientProfileId);
    List<AppointmentDTO> getDoctorUpcomingAppointments(Long doctorProfileId);
    
    // Availability and Conflict Checking
    boolean isDoctorAvailable(Long doctorProfileId, LocalDateTime startTime, LocalDateTime endTime);
    List<AppointmentDTO> checkConflictingAppointments(Long doctorProfileId, LocalDateTime startTime, LocalDateTime endTime);
    
    // Dashboard and Reports
    List<AppointmentDTO> getOverdueAppointments();
    List<AppointmentDTO> getAppointmentsWithSevereVitals();
    List<AppointmentDTO> getMonthlyAppointments(int year, int month);
    
    // Statistics
    Long countAppointmentsByStatus(AppointmentStatus status);
    Long countPatientAppointments(Long patientProfileId);
    Long countDoctorAppointments(Long doctorProfileId);
    
    // Vital Signs Management
    VitalSignsDTO getAppointmentVitalSigns(Long appointmentId);
    VitalSignsDTO updateVitalSigns(Long appointmentId, VitalSignsDTO vitalSignsDTO);
    List<VitalSignsDTO> getPatientVitalSignsHistory(Long patientProfileId);
}