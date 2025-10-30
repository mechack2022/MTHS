package com.mths.consultation.controller;

import com.mths.shared.constants.AppointmentStatus;
import com.mths.consultation.dto.*;
import com.mths.patient.dto.VitalSignsDTO;
import com.mths.shared.dto.ApiResponse;
import com.mths.consultation.service.AppointmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/v1/appointments")
@Slf4j
@RequiredArgsConstructor
public class AppointmentController {

    private final AppointmentService appointmentService;

    // ========================================================================
    // APPOINTMENT MANAGEMENT
    // ========================================================================

    @PostMapping
    @PreAuthorize("hasRole('PATIENT') or hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<AppointmentDTO>> createAppointment(
            @Valid @RequestBody CreateAppointmentRequest request) {
        
        log.info("Creating appointment for patient: {} with doctor: {}", 
                request.getPatientProfileId(), request.getDoctorProfileId());

        AppointmentDTO appointment = appointmentService.createAppointment(request);

        ApiResponse<AppointmentDTO> response = ApiResponse.success(
                "Appointment scheduled successfully",
                appointment,
                HttpStatus.CREATED.value()
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{appointmentId}")
    @PreAuthorize("hasRole('PATIENT') or hasRole('DOCTOR') or hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<AppointmentDTO>> getAppointment(
            @PathVariable Long appointmentId) {
        
        AppointmentDTO appointment = appointmentService.getAppointmentById(appointmentId);

        ApiResponse<AppointmentDTO> response = ApiResponse.success(
                "Appointment retrieved successfully",
                appointment,
                HttpStatus.OK.value()
        );

        return ResponseEntity.ok(response);
    }

    @PutMapping("/{appointmentId}")
    @PreAuthorize("hasRole('DOCTOR') or hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<AppointmentDTO>> updateAppointment(
            @PathVariable Long appointmentId,
            @Valid @RequestBody UpdateAppointmentRequest request) {
        
        log.info("Updating appointment: {}", appointmentId);

        AppointmentDTO appointment = appointmentService.updateAppointment(appointmentId, request);

        ApiResponse<AppointmentDTO> response = ApiResponse.success(
                "Appointment updated successfully",
                appointment,
                HttpStatus.OK.value()
        );

        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{appointmentId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<String>> deleteAppointment(
            @PathVariable Long appointmentId) {
        
        log.info("Deleting appointment: {}", appointmentId);

        appointmentService.deleteAppointment(appointmentId);

        ApiResponse<String> response = ApiResponse.success(
                "Appointment deleted successfully"
        );

        return ResponseEntity.ok(response);
    }

    // ========================================================================
    // APPOINTMENT ACTIONS
    // ========================================================================

    @PutMapping("/{appointmentId}/reschedule")
    @PreAuthorize("hasRole('PATIENT') or hasRole('DOCTOR') or hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<AppointmentDTO>> rescheduleAppointment(
            @Valid @RequestBody RescheduleAppointmentRequest request) {
        
        log.info("Rescheduling appointment: {}", request.getAppointmentId());

        AppointmentDTO appointment = appointmentService.rescheduleAppointment(request);

        ApiResponse<AppointmentDTO> response = ApiResponse.success(
                "Appointment rescheduled successfully",
                appointment,
                HttpStatus.OK.value()
        );

        return ResponseEntity.ok(response);
    }

    @PutMapping("/{appointmentId}/cancel")
    @PreAuthorize("hasRole('PATIENT') or hasRole('DOCTOR') or hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<AppointmentDTO>> cancelAppointment(
            @Valid @RequestBody CancelAppointmentRequest request) {
        
        log.info("Cancelling appointment: {}", request.getAppointmentId());

        AppointmentDTO appointment = appointmentService.cancelAppointment(request);

        ApiResponse<AppointmentDTO> response = ApiResponse.success(
                "Appointment cancelled successfully",
                appointment,
                HttpStatus.OK.value()
        );

        return ResponseEntity.ok(response);
    }

    @PutMapping("/{appointmentId}/confirm")
    @PreAuthorize("hasRole('DOCTOR') or hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<AppointmentDTO>> confirmAppointment(
            @PathVariable Long appointmentId) {
        
        log.info("Confirming appointment: {}", appointmentId);

        AppointmentDTO appointment = appointmentService.confirmAppointment(appointmentId);

        ApiResponse<AppointmentDTO> response = ApiResponse.success(
                "Appointment confirmed successfully",
                appointment,
                HttpStatus.OK.value()
        );

        return ResponseEntity.ok(response);
    }

    @PutMapping("/{appointmentId}/start")
    @PreAuthorize("hasRole('DOCTOR') or hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<AppointmentDTO>> startConsultation(
            @PathVariable Long appointmentId) {
        
        log.info("Starting consultation for appointment: {}", appointmentId);

        AppointmentDTO appointment = appointmentService.startConsultation(appointmentId);

        ApiResponse<AppointmentDTO> response = ApiResponse.success(
                "Consultation started successfully",
                appointment,
                HttpStatus.OK.value()
        );

        return ResponseEntity.ok(response);
    }

    @PutMapping("/{appointmentId}/complete")
    @PreAuthorize("hasRole('DOCTOR') or hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<AppointmentDTO>> completeAppointment(
            @PathVariable Long appointmentId,
            @RequestBody CompleteAppointmentRequest request) {
        
        log.info("Completing appointment: {}", appointmentId);

        AppointmentDTO appointment = appointmentService.completeAppointment(
                appointmentId, request.getConsultationNotes(), request.getDiagnosis());

        ApiResponse<AppointmentDTO> response = ApiResponse.success(
                "Appointment completed successfully",
                appointment,
                HttpStatus.OK.value()
        );

        return ResponseEntity.ok(response);
    }

    // ========================================================================
    // APPOINTMENT QUERIES
    // ========================================================================

    @GetMapping("/patient/{patientProfileId}")
    @PreAuthorize("hasRole('PATIENT') or hasRole('DOCTOR') or hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<List<AppointmentDTO>>> getPatientAppointments(
            @PathVariable Long patientProfileId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        if (size > 0) {
            Pageable pageable = PageRequest.of(page, size);
            Page<AppointmentDTO> appointments = appointmentService.getPatientAppointments(patientProfileId, pageable);
            
            ApiResponse<List<AppointmentDTO>> response = ApiResponse.success(
                    "Patient appointments retrieved successfully",
                    appointments.getContent(),
                    HttpStatus.OK.value()
            );
            return ResponseEntity.ok(response);
        } else {
            List<AppointmentDTO> appointments = appointmentService.getPatientAppointments(patientProfileId);
            
            ApiResponse<List<AppointmentDTO>> response = ApiResponse.success(
                    "Patient appointments retrieved successfully",
                    appointments,
                    HttpStatus.OK.value()
            );
            return ResponseEntity.ok(response);
        }
    }

    @GetMapping("/doctor/{doctorProfileId}")
    @PreAuthorize("hasRole('DOCTOR') or hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<List<AppointmentDTO>>> getDoctorAppointments(
            @PathVariable Long doctorProfileId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        if (size > 0) {
            Pageable pageable = PageRequest.of(page, size);
            Page<AppointmentDTO> appointments = appointmentService.getDoctorAppointments(doctorProfileId, pageable);
            
            ApiResponse<List<AppointmentDTO>> response = ApiResponse.success(
                    "Doctor appointments retrieved successfully",
                    appointments.getContent(),
                    HttpStatus.OK.value()
            );
            return ResponseEntity.ok(response);
        } else {
            List<AppointmentDTO> appointments = appointmentService.getDoctorAppointments(doctorProfileId);
            
            ApiResponse<List<AppointmentDTO>> response = ApiResponse.success(
                    "Doctor appointments retrieved successfully",
                    appointments,
                    HttpStatus.OK.value()
            );
            return ResponseEntity.ok(response);
        }
    }

    @GetMapping("/patient/{patientProfileId}/upcoming")
    @PreAuthorize("hasRole('PATIENT') or hasRole('DOCTOR') or hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<List<AppointmentDTO>>> getPatientUpcomingAppointments(
            @PathVariable Long patientProfileId) {
        
        List<AppointmentDTO> appointments = appointmentService.getPatientUpcomingAppointments(patientProfileId);

        ApiResponse<List<AppointmentDTO>> response = ApiResponse.success(
                "Patient upcoming appointments retrieved successfully",
                appointments,
                HttpStatus.OK.value()
        );

        return ResponseEntity.ok(response);
    }

    @GetMapping("/doctor/{doctorProfileId}/upcoming")
    @PreAuthorize("hasRole('DOCTOR') or hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<List<AppointmentDTO>>> getDoctorUpcomingAppointments(
            @PathVariable Long doctorProfileId) {
        
        List<AppointmentDTO> appointments = appointmentService.getDoctorUpcomingAppointments(doctorProfileId);

        ApiResponse<List<AppointmentDTO>> response = ApiResponse.success(
                "Doctor upcoming appointments retrieved successfully",
                appointments,
                HttpStatus.OK.value()
        );

        return ResponseEntity.ok(response);
    }

    // ========================================================================
    // VITAL SIGNS MANAGEMENT
    // ========================================================================

    @GetMapping("/{appointmentId}/vital-signs")
    @PreAuthorize("hasRole('PATIENT') or hasRole('DOCTOR') or hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<VitalSignsDTO>> getAppointmentVitalSigns(
            @PathVariable Long appointmentId) {
        
        VitalSignsDTO vitalSigns = appointmentService.getAppointmentVitalSigns(appointmentId);

        ApiResponse<VitalSignsDTO> response = ApiResponse.success(
                "Vital signs retrieved successfully",
                vitalSigns,
                HttpStatus.OK.value()
        );

        return ResponseEntity.ok(response);
    }

    @PutMapping("/{appointmentId}/vital-signs")
    @PreAuthorize("hasRole('DOCTOR') or hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<VitalSignsDTO>> updateVitalSigns(
            @PathVariable Long appointmentId,
            @Valid @RequestBody VitalSignsDTO vitalSignsDTO) {
        
        log.info("Updating vital signs for appointment: {}", appointmentId);

        VitalSignsDTO updatedVitalSigns = appointmentService.updateVitalSigns(appointmentId, vitalSignsDTO);

        ApiResponse<VitalSignsDTO> response = ApiResponse.success(
                "Vital signs updated successfully",
                updatedVitalSigns,
                HttpStatus.OK.value()
        );

        return ResponseEntity.ok(response);
    }

    @GetMapping("/patient/{patientProfileId}/vital-signs-history")
    @PreAuthorize("hasRole('PATIENT') or hasRole('DOCTOR') or hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<List<VitalSignsDTO>>> getPatientVitalSignsHistory(
            @PathVariable Long patientProfileId) {
        
        List<VitalSignsDTO> vitalSignsHistory = appointmentService.getPatientVitalSignsHistory(patientProfileId);

        ApiResponse<List<VitalSignsDTO>> response = ApiResponse.success(
                "Patient vital signs history retrieved successfully",
                vitalSignsHistory,
                HttpStatus.OK.value()
        );

        return ResponseEntity.ok(response);
    }

    // ========================================================================
    // AVAILABILITY AND SCHEDULING
    // ========================================================================

    @GetMapping("/doctor/{doctorProfileId}/availability")
    @PreAuthorize("hasRole('PATIENT') or hasRole('DOCTOR') or hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<Boolean>> checkDoctorAvailability(
            @PathVariable Long doctorProfileId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime) {
        
        boolean isAvailable = appointmentService.isDoctorAvailable(doctorProfileId, startTime, endTime);

        ApiResponse<Boolean> response = ApiResponse.success(
                "Doctor availability checked successfully",
                isAvailable,
                HttpStatus.OK.value()
        );

        return ResponseEntity.ok(response);
    }

    @GetMapping("/doctor/{doctorProfileId}/conflicts")
    @PreAuthorize("hasRole('DOCTOR') or hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<List<AppointmentDTO>>> getConflictingAppointments(
            @PathVariable Long doctorProfileId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime) {
        
        List<AppointmentDTO> conflicts = appointmentService.checkConflictingAppointments(doctorProfileId, startTime, endTime);

        ApiResponse<List<AppointmentDTO>> response = ApiResponse.success(
                "Conflicting appointments retrieved successfully",
                conflicts,
                HttpStatus.OK.value()
        );

        return ResponseEntity.ok(response);
    }

    // ========================================================================
    // REPORTS AND STATISTICS
    // ========================================================================

    @GetMapping("/status/{status}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<List<AppointmentDTO>>> getAppointmentsByStatus(
            @PathVariable AppointmentStatus status) {
        
        List<AppointmentDTO> appointments = appointmentService.getAppointmentsByStatus(status);

        ApiResponse<List<AppointmentDTO>> response = ApiResponse.success(
                "Appointments by status retrieved successfully",
                appointments,
                HttpStatus.OK.value()
        );

        return ResponseEntity.ok(response);
    }

    @GetMapping("/overdue")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<List<AppointmentDTO>>> getOverdueAppointments() {
        
        List<AppointmentDTO> overdueAppointments = appointmentService.getOverdueAppointments();

        ApiResponse<List<AppointmentDTO>> response = ApiResponse.success(
                "Overdue appointments retrieved successfully",
                overdueAppointments,
                HttpStatus.OK.value()
        );

        return ResponseEntity.ok(response);
    }

    @GetMapping("/severe-vitals")
    @PreAuthorize("hasRole('DOCTOR') or hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<List<AppointmentDTO>>> getAppointmentsWithSevereVitals() {
        
        List<AppointmentDTO> appointments = appointmentService.getAppointmentsWithSevereVitals();

        ApiResponse<List<AppointmentDTO>> response = ApiResponse.success(
                "Appointments with severe vitals retrieved successfully",
                appointments,
                HttpStatus.OK.value()
        );

        return ResponseEntity.ok(response);
    }

    @GetMapping("/monthly/{year}/{month}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<List<AppointmentDTO>>> getMonthlyAppointments(
            @PathVariable int year,
            @PathVariable int month) {
        
        List<AppointmentDTO> appointments = appointmentService.getMonthlyAppointments(year, month);

        ApiResponse<List<AppointmentDTO>> response = ApiResponse.success(
                "Monthly appointments retrieved successfully",
                appointments,
                HttpStatus.OK.value()
        );

        return ResponseEntity.ok(response);
    }

    // ========================================================================
    // STATISTICS
    // ========================================================================

    @GetMapping("/statistics/count-by-status/{status}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<Long>> countAppointmentsByStatus(
            @PathVariable AppointmentStatus status) {
        
        Long count = appointmentService.countAppointmentsByStatus(status);

        ApiResponse<Long> response = ApiResponse.success(
                "Appointment count retrieved successfully",
                count,
                HttpStatus.OK.value()
        );

        return ResponseEntity.ok(response);
    }

    // ========================================================================
    // REQUEST/RESPONSE RECORDS
    // ========================================================================

    public record CompleteAppointmentRequest(String consultationNotes, String diagnosis) {
        public String getConsultationNotes() {
            return consultationNotes;
        }
        
        public String getDiagnosis() {
            return diagnosis;
        }
    }
}