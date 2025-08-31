package com.auth.service.service;

import com.auth.service.constants.AppointmentStatus;
import com.auth.service.dto.*;
import com.auth.service.entity.*;
import com.auth.service.exceptions.BadRequestException;
import com.auth.service.exceptions.ResourceNotFoundException;
import com.auth.service.mapper.AppointmentMapper;
import com.auth.service.repository.*;
import com.auth.service.service.AccountValidationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class AppointmentServiceImpl implements AppointmentService {

    private final AppointmentRepository appointmentRepository;
    private final VitalSignsRepository vitalSignsRepository;
    private final AppointmentMapper appointmentMapper;
    private final AccountValidationService accountValidationService;

    @Override
    @Transactional
    public AppointmentDTO createAppointment(CreateAppointmentRequest request) {
        log.info("Creating appointment for patient: {} with doctor: {}", 
                request.getPatientProfileId(), request.getDoctorProfileId());

        // Validate patient and doctor accounts
        accountValidationService.validatePatientAccount(request.getPatientProfileId());
        accountValidationService.validateDoctorAccount(request.getDoctorProfileId());

        // Validate scheduling conflicts
        LocalDateTime endTime = request.getScheduledDatetime().plusMinutes(request.getDurationMinutes());
        if (!isDoctorAvailable(request.getDoctorProfileId(), request.getScheduledDatetime(), endTime)) {
            throw new BadRequestException("scheduling", "Doctor is not available at the requested time");
        }

        // Create appointment
        Appointment appointment = new Appointment();
        appointment.setPatientProfileId(request.getPatientProfileId());
        appointment.setDoctorProfileId(request.getDoctorProfileId());
        appointment.setAppointmentType(request.getAppointmentType());
        appointment.setScheduledDatetime(request.getScheduledDatetime());
        appointment.setDurationMinutes(request.getDurationMinutes());
        appointment.setSymptoms(request.getSymptoms());
        appointment.setConsultationFee(request.getConsultationFee());
        appointment.setStatus(AppointmentStatus.SCHEDULED);

        Appointment savedAppointment = appointmentRepository.save(appointment);

        // Create vital signs if provided
        if (hasVitalSignsData(request)) {
            createVitalSigns(savedAppointment.getId(), request);
        }

        log.info("Appointment created successfully with ID: {}", savedAppointment.getId());
        return appointmentMapper.mapToAppointmentDTO(savedAppointment);
    }

    @Override
    @Transactional(readOnly = true)
    public AppointmentDTO getAppointmentById(Long appointmentId) {
        Appointment appointment = findAppointmentById(appointmentId);
        return appointmentMapper.mapToAppointmentDTO(appointment);
    }

    @Override
    @Transactional
    public AppointmentDTO updateAppointment(Long appointmentId, UpdateAppointmentRequest request) {
        log.info("Updating appointment: {}", appointmentId);

        Appointment appointment = findAppointmentById(appointmentId);

        // Update appointment fields
        if (request.getStatus() != null) {
            appointment.setStatus(request.getStatus());
        }
        if (request.getConsultationNotes() != null) {
            appointment.setConsultationNotes(request.getConsultationNotes());
        }
        if (request.getDiagnosis() != null) {
            appointment.setDiagnosis(request.getDiagnosis());
        }
        if (request.getMeetingUrl() != null) {
            appointment.setMeetingUrl(request.getMeetingUrl());
        }
        if (request.getMeetingId() != null) {
            appointment.setMeetingId(request.getMeetingId());
        }

        Appointment updatedAppointment = appointmentRepository.save(appointment);

        // Update vital signs if provided
        if (hasVitalSignsDataInUpdate(request)) {
            updateAppointmentVitalSigns(appointmentId, request);
        }

        log.info("Appointment updated successfully: {}", appointmentId);
        return appointmentMapper.mapToAppointmentDTO(updatedAppointment);
    }

    @Override
    @Transactional
    public void deleteAppointment(Long appointmentId) {
        log.info("Deleting appointment: {}", appointmentId);
        
        Appointment appointment = findAppointmentById(appointmentId);
        
        if (!appointment.canBeCancelled()) {
            throw new BadRequestException("appointment", "Cannot delete appointment with status: " + appointment.getStatus());
        }
        
        appointmentRepository.delete(appointment);
        log.info("Appointment deleted successfully: {}", appointmentId);
    }

    @Override
    @Transactional
    public AppointmentDTO rescheduleAppointment(RescheduleAppointmentRequest request) {
        log.info("Rescheduling appointment: {} to new time: {}", 
                request.getAppointmentId(), request.getNewDatetime());

        Appointment appointment = findAppointmentById(request.getAppointmentId());

        if (!appointment.canBeRescheduled()) {
            throw new BadRequestException("appointment", 
                    "Cannot reschedule appointment with status: " + appointment.getStatus());
        }

        // Check doctor availability for new time
        LocalDateTime newEndTime = request.getNewDatetime().plusMinutes(appointment.getDurationMinutes());
        if (!isDoctorAvailable(appointment.getDoctorProfileId(), request.getNewDatetime(), newEndTime)) {
            throw new BadRequestException("scheduling", "Doctor is not available at the new requested time");
        }

        // Create reschedule record
        AppointmentReschedule reschedule = new AppointmentReschedule();
        reschedule.setAppointmentId(appointment.getId());
        reschedule.setOldDatetime(appointment.getScheduledDatetime());
        reschedule.setNewDatetime(request.getNewDatetime());
        reschedule.setReason(request.getReason());
        reschedule.setRescheduledBy(request.getRescheduledBy());

        appointment.getReschedules().add(reschedule);

        // Update appointment
        appointment.setScheduledDatetime(request.getNewDatetime());
        appointment.setStatus(AppointmentStatus.RESCHEDULED);

        Appointment savedAppointment = appointmentRepository.save(appointment);

        log.info("Appointment rescheduled successfully: {}", appointment.getId());
        return appointmentMapper.mapToAppointmentDTO(savedAppointment);
    }

    @Override
    @Transactional
    public AppointmentDTO cancelAppointment(CancelAppointmentRequest request) {
        log.info("Cancelling appointment: {} for reason: {}", 
                request.getAppointmentId(), request.getReason());

        Appointment appointment = findAppointmentById(request.getAppointmentId());

        if (!appointment.canBeCancelled()) {
            throw new BadRequestException("appointment", 
                    "Cannot cancel appointment with status: " + appointment.getStatus());
        }

        appointment.setStatus(AppointmentStatus.CANCELLED);
        appointment.setCancelledReason(request.getReason());
        appointment.setCancelledBy(request.getCancelledBy());
        appointment.setCancelledAt(LocalDateTime.now());

        Appointment savedAppointment = appointmentRepository.save(appointment);

        log.info("Appointment cancelled successfully: {}", appointment.getId());
        return appointmentMapper.mapToAppointmentDTO(savedAppointment);
    }

    @Override
    @Transactional
    public AppointmentDTO confirmAppointment(Long appointmentId) {
        log.info("Confirming appointment: {}", appointmentId);

        Appointment appointment = findAppointmentById(appointmentId);

        if (appointment.getStatus() != AppointmentStatus.SCHEDULED) {
            throw new BadRequestException("appointment", 
                    "Can only confirm scheduled appointments. Current status: " + appointment.getStatus());
        }

        appointment.setStatus(AppointmentStatus.CONFIRMED);
        Appointment savedAppointment = appointmentRepository.save(appointment);

        log.info("Appointment confirmed successfully: {}", appointmentId);
        return appointmentMapper.mapToAppointmentDTO(savedAppointment);
    }

    @Override
    @Transactional
    public AppointmentDTO startConsultation(Long appointmentId) {
        log.info("Starting consultation for appointment: {}", appointmentId);

        Appointment appointment = findAppointmentById(appointmentId);

        if (appointment.getStatus() != AppointmentStatus.CONFIRMED && 
            appointment.getStatus() != AppointmentStatus.SCHEDULED) {
            throw new BadRequestException("appointment", 
                    "Can only start confirmed or scheduled appointments. Current status: " + appointment.getStatus());
        }

        appointment.setStatus(AppointmentStatus.IN_PROGRESS);
        Appointment savedAppointment = appointmentRepository.save(appointment);

        log.info("Consultation started successfully for appointment: {}", appointmentId);
        return appointmentMapper.mapToAppointmentDTO(savedAppointment);
    }

    @Override
    @Transactional
    public AppointmentDTO completeAppointment(Long appointmentId, String consultationNotes, String diagnosis) {
        log.info("Completing appointment: {}", appointmentId);

        Appointment appointment = findAppointmentById(appointmentId);

        if (appointment.getStatus() != AppointmentStatus.IN_PROGRESS) {
            throw new BadRequestException("appointment", 
                    "Can only complete appointments that are in progress. Current status: " + appointment.getStatus());
        }

        appointment.setStatus(AppointmentStatus.COMPLETED);
        appointment.setConsultationNotes(consultationNotes);
        appointment.setDiagnosis(diagnosis);

        Appointment savedAppointment = appointmentRepository.save(appointment);

        log.info("Appointment completed successfully: {}", appointmentId);
        return appointmentMapper.mapToAppointmentDTO(savedAppointment);
    }

    // Query Methods
    @Override
    @Transactional(readOnly = true)
    public List<AppointmentDTO> getPatientAppointments(Long patientProfileId) {
        return appointmentRepository.findByPatientProfileIdOrderByScheduledDatetimeDesc(patientProfileId)
                .stream()
                .map(appointmentMapper::mapToAppointmentDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<AppointmentDTO> getDoctorAppointments(Long doctorProfileId) {
        return appointmentRepository.findByDoctorProfileIdOrderByScheduledDatetimeDesc(doctorProfileId)
                .stream()
                .map(appointmentMapper::mapToAppointmentDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AppointmentDTO> getPatientAppointments(Long patientProfileId, Pageable pageable) {
        return appointmentRepository.findByPatientProfileIdOrderByScheduledDatetimeDesc(patientProfileId, pageable)
                .map(appointmentMapper::mapToAppointmentDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AppointmentDTO> getDoctorAppointments(Long doctorProfileId, Pageable pageable) {
        return appointmentRepository.findByDoctorProfileIdOrderByScheduledDatetimeDesc(doctorProfileId, pageable)
                .map(appointmentMapper::mapToAppointmentDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isDoctorAvailable(Long doctorProfileId, LocalDateTime startTime, LocalDateTime endTime) {
        List<Appointment> conflicts = appointmentRepository.findConflictingAppointments(
                doctorProfileId, startTime, endTime);
        return conflicts.isEmpty();
    }

    @Override
    @Transactional(readOnly = true)
    public List<AppointmentDTO> checkConflictingAppointments(Long doctorProfileId, LocalDateTime startTime, LocalDateTime endTime) {
        return appointmentRepository.findConflictingAppointments(doctorProfileId, startTime, endTime)
                .stream()
                .map(appointmentMapper::mapToAppointmentDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<AppointmentDTO> getPatientUpcomingAppointments(Long patientProfileId) {
        return appointmentRepository.findPatientUpcomingAppointments(patientProfileId, LocalDateTime.now())
                .stream()
                .map(appointmentMapper::mapToAppointmentDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<AppointmentDTO> getDoctorUpcomingAppointments(Long doctorProfileId) {
        return appointmentRepository.findDoctorUpcomingAppointments(doctorProfileId, LocalDateTime.now())
                .stream()
                .map(appointmentMapper::mapToAppointmentDTO)
                .collect(Collectors.toList());
    }

    // Vital Signs Methods
    @Override
    @Transactional(readOnly = true)
    public VitalSignsDTO getAppointmentVitalSigns(Long appointmentId) {
        return vitalSignsRepository.findByAppointmentId(appointmentId)
                .map(appointmentMapper::mapToVitalSignsDTO)
                .orElse(null);
    }

    @Override
    @Transactional
    public VitalSignsDTO updateVitalSigns(Long appointmentId, VitalSignsDTO vitalSignsDTO) {
        VitalSigns vitalSigns = vitalSignsRepository.findByAppointmentId(appointmentId)
                .orElseThrow(() -> new ResourceNotFoundException("VitalSigns", "appointmentId", appointmentId));

        // Update vital signs
        if (vitalSignsDTO.getBloodPressure() != null) {
            vitalSigns.setBloodPressure(vitalSignsDTO.getBloodPressure());
        }
        if (vitalSignsDTO.getHeartRate() != null) {
            vitalSigns.setHeartRate(vitalSignsDTO.getHeartRate());
        }
        if (vitalSignsDTO.getTemperature() != null) {
            vitalSigns.setTemperature(vitalSignsDTO.getTemperature());
        }
        if (vitalSignsDTO.getWeight() != null) {
            vitalSigns.setWeight(vitalSignsDTO.getWeight());
        }
        if (vitalSignsDTO.getHeight() != null) {
            vitalSigns.setHeight(vitalSignsDTO.getHeight());
        }
        if (vitalSignsDTO.getNotes() != null) {
            vitalSigns.setNotes(vitalSignsDTO.getNotes());
        }

        VitalSigns savedVitalSigns = vitalSignsRepository.save(vitalSigns);
        return appointmentMapper.mapToVitalSignsDTO(savedVitalSigns);
    }

    @Override
    @Transactional(readOnly = true)
    public List<VitalSignsDTO> getPatientVitalSignsHistory(Long patientProfileId) {
        return vitalSignsRepository.findPatientVitalSignsHistory(patientProfileId)
                .stream()
                .map(appointmentMapper::mapToVitalSignsDTO)
                .collect(Collectors.toList());
    }

    // Statistics and Reports Methods
    @Override
    @Transactional(readOnly = true)
    public List<AppointmentDTO> getAppointmentsByStatus(AppointmentStatus status) {
        return appointmentRepository.findByStatus(status)
                .stream()
                .map(appointmentMapper::mapToAppointmentDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<AppointmentDTO> getPatientAppointmentsByStatus(Long patientProfileId, AppointmentStatus status) {
        return appointmentRepository.findByPatientProfileIdAndStatus(patientProfileId, status)
                .stream()
                .map(appointmentMapper::mapToAppointmentDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<AppointmentDTO> getDoctorAppointmentsByStatus(Long doctorProfileId, AppointmentStatus status) {
        return appointmentRepository.findByDoctorProfileIdAndStatus(doctorProfileId, status)
                .stream()
                .map(appointmentMapper::mapToAppointmentDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<AppointmentDTO> getAppointmentsByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        return appointmentRepository.findAppointmentsByDateRange(startDate, endDate)
                .stream()
                .map(appointmentMapper::mapToAppointmentDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<AppointmentDTO> getDoctorAppointmentsForDate(Long doctorProfileId, LocalDateTime date) {
        return appointmentRepository.findDoctorAppointmentsForDate(doctorProfileId, date)
                .stream()
                .map(appointmentMapper::mapToAppointmentDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<AppointmentDTO> getOverdueAppointments() {
        return appointmentRepository.findOverdueAppointments(LocalDateTime.now())
                .stream()
                .map(appointmentMapper::mapToAppointmentDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<AppointmentDTO> getAppointmentsWithSevereVitals() {
        return appointmentRepository.findAppointmentsWithSevereVitals()
                .stream()
                .map(appointmentMapper::mapToAppointmentDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<AppointmentDTO> getMonthlyAppointments(int year, int month) {
        return appointmentRepository.findAppointmentsByMonth(year, month)
                .stream()
                .map(appointmentMapper::mapToAppointmentDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Long countAppointmentsByStatus(AppointmentStatus status) {
        return appointmentRepository.countByStatus(status);
    }

    @Override
    @Transactional(readOnly = true)
    public Long countPatientAppointments(Long patientProfileId) {
        return appointmentRepository.countByPatientProfileId(patientProfileId);
    }

    @Override
    @Transactional(readOnly = true)
    public Long countDoctorAppointments(Long doctorProfileId) {
        return appointmentRepository.countByDoctorProfileId(doctorProfileId);
    }

    // Helper Methods
    private Appointment findAppointmentById(Long appointmentId) {
        return appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Appointment", "appointmentId", appointmentId));
    }

    private boolean hasVitalSignsData(CreateAppointmentRequest request) {
        return request.getBloodPressure() != null || 
               request.getHeartRate() != null || 
               request.getTemperature() != null ||
               request.getWeight() != null || 
               request.getHeight() != null;
    }

    private boolean hasVitalSignsDataInUpdate(UpdateAppointmentRequest request) {
        return request.getBloodPressure() != null || 
               request.getHeartRate() != null || 
               request.getTemperature() != null ||
               request.getWeight() != null || 
               request.getHeight() != null;
    }

    private void createVitalSigns(Long appointmentId, CreateAppointmentRequest request) {
        VitalSigns vitalSigns = new VitalSigns();
        vitalSigns.setAppointmentId(appointmentId);
        vitalSigns.setBloodPressure(request.getBloodPressure());
        vitalSigns.setHeartRate(request.getHeartRate());
        vitalSigns.setTemperature(request.getTemperature());
        vitalSigns.setWeight(request.getWeight());
        vitalSigns.setHeight(request.getHeight());
        vitalSigns.setNotes(request.getVitalSignsNotes());
        vitalSigns.setRecordedBy("patient"); // Or get from security context

        vitalSignsRepository.save(vitalSigns);
    }

    private void updateAppointmentVitalSigns(Long appointmentId, UpdateAppointmentRequest request) {
        VitalSigns vitalSigns = vitalSignsRepository.findByAppointmentId(appointmentId)
                .orElse(new VitalSigns());

        if (vitalSigns.getId() == null) {
            vitalSigns.setAppointmentId(appointmentId);
        }

        if (request.getBloodPressure() != null) {
            vitalSigns.setBloodPressure(request.getBloodPressure());
        }
        if (request.getHeartRate() != null) {
            vitalSigns.setHeartRate(request.getHeartRate());
        }
        if (request.getTemperature() != null) {
            vitalSigns.setTemperature(request.getTemperature());
        }
        if (request.getWeight() != null) {
            vitalSigns.setWeight(request.getWeight());
        }
        if (request.getHeight() != null) {
            vitalSigns.setHeight(request.getHeight());
        }
        if (request.getVitalSignsNotes() != null) {
            vitalSigns.setNotes(request.getVitalSignsNotes());
        }

        vitalSignsRepository.save(vitalSigns);
    }
}