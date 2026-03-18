package com.mths.shared.mapper;

import com.mths.consultation.dto.AppointmentDTO;
import com.mths.consultation.entity.Appointment;
import com.mths.consultation.repository.AppointmentRepository;
import com.mths.patient.dto.VitalSignsDTO;
import com.mths.patient.entity.VitalSigns;
import com.mths.patient.entity.PatientProfile;
import com.mths.hospital.entity.DoctorProfile;
import com.mths.patient.repository.PatientProfileRepository;
import com.mths.hospital.repository.DoctorProfileRepository;
import com.mths.shared.exceptions.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AppointmentMapper {

    private final PatientProfileRepository patientProfileRepository;
    private final DoctorProfileRepository doctorProfileRepository;
    private final AppointmentRepository appointmentRepository;

    public AppointmentDTO mapToAppointmentDTO(Appointment appointment) {
        if (appointment == null) {
            return null;
        }

        AppointmentDTO dto = new AppointmentDTO();
        dto.setId(appointment.getId());
        dto.setPatientProfileId(appointment.getPatientProfileId());
        dto.setDoctorProfileId(appointment.getDoctorProfileId());
        dto.setAppointmentType(appointment.getAppointmentType());
        dto.setScheduledDatetime(appointment.getScheduledDatetime());
        dto.setStatus(appointment.getStatus());
        dto.setConsultationNotes(appointment.getConsultationNotes());
        dto.setSymptoms(appointment.getSymptoms());
        dto.setDiagnosis(appointment.getDiagnosis());
        dto.setConsultationFee(appointment.getConsultationFee());
        dto.setDurationMinutes(appointment.getDurationMinutes());
        dto.setMeetingUrl(appointment.getMeetingUrl());
        dto.setMeetingId(appointment.getMeetingId());
        dto.setCancelledReason(appointment.getCancelledReason());
        dto.setCancelledBy(appointment.getCancelledBy());
        dto.setCancelledAt(appointment.getCancelledAt());
        dto.setCreatedAt(appointment.getCreatedAt());
        dto.setUpdatedAt(appointment.getUpdatedAt());

        // Set helper fields
        dto.setCanBeRescheduled(appointment.canBeRescheduled());
        dto.setCanBeCancelled(appointment.canBeCancelled());

        // Load vital signs if exists
        VitalSigns vitalSigns = appointment.getVitalSigns();
        if (vitalSigns != null) {
            dto.setVitalSigns(mapToVitalSignsDTO(vitalSigns));
        }

        return dto;
    }

    public VitalSignsDTO mapToVitalSignsDTO(VitalSigns vitalSigns) {
        if (vitalSigns == null) {
            return null;
        }

        VitalSignsDTO dto = new VitalSignsDTO();
        dto.setId(vitalSigns.getId());
        dto.setPatientProfileId(vitalSigns.getPatientProfileId());
        dto.setAppointmentId(vitalSigns.getAppointmentId());
        dto.setBloodPressure(vitalSigns.getBloodPressure());
        dto.setHeartRate(vitalSigns.getHeartRate());
        dto.setTemperature(vitalSigns.getTemperature());
        dto.setWeight(vitalSigns.getWeight());
        dto.setHeight(vitalSigns.getHeight());
        dto.setBmi(vitalSigns.getBmi());
        dto.setRecordedAt(vitalSigns.getRecordedAt());

        // Set recorder information
        dto.setRecordedByRole(vitalSigns.getRecordedByRole());
        Long userId = vitalSigns.getRecordedByUserId();
        dto.setRecordedByUserId(userId != null ? userId.toString() : null);
        dto.setRecordedByName(vitalSigns.getRecordedByName());

        dto.setNotes(vitalSigns.getNotes());
        dto.setHealthStatus(vitalSigns.getHealthStatus());

        // Set helper fields
        if (vitalSigns.getHealthStatus() != null) {
            dto.setHealthStatusDescription(vitalSigns.getHealthStatus().getDescription());
            dto.setHealthStatusColor(vitalSigns.getHealthStatus().getColor());
        }
        dto.setSystolicPressure(vitalSigns.getSystolicPressure());
        dto.setDiastolicPressure(vitalSigns.getDiastolicPressure());

        return dto;
    }

    public Appointment mapToAppointment(AppointmentDTO dto) {
        if (dto == null) {
            return null;
        }

        Appointment appointment = new Appointment();
        // ID is auto-generated, don't set it manually
        
        // Set profile relationships if IDs are provided
        if (dto.getPatientProfileId() != null) {
            PatientProfile patientProfile = patientProfileRepository.findById(dto.getPatientProfileId())
                    .orElseThrow(() -> new ResourceNotFoundException("PatientProfile", "id", dto.getPatientProfileId()));
            appointment.setPatientProfile(patientProfile);
        }
        
        if (dto.getDoctorProfileId() != null) {
            DoctorProfile doctorProfile = doctorProfileRepository.findById(dto.getDoctorProfileId())
                    .orElseThrow(() -> new ResourceNotFoundException("DoctorProfile", "id", dto.getDoctorProfileId()));
            appointment.setDoctorProfile(doctorProfile);
        }
        
        appointment.setAppointmentType(dto.getAppointmentType());
        appointment.setScheduledDatetime(dto.getScheduledDatetime());
        appointment.setStatus(dto.getStatus());
        appointment.setConsultationNotes(dto.getConsultationNotes());
        appointment.setSymptoms(dto.getSymptoms());
        appointment.setDiagnosis(dto.getDiagnosis());
        appointment.setConsultationFee(dto.getConsultationFee());
        appointment.setDurationMinutes(dto.getDurationMinutes());
        appointment.setMeetingUrl(dto.getMeetingUrl());
        appointment.setMeetingId(dto.getMeetingId());
        appointment.setCancelledReason(dto.getCancelledReason());
        appointment.setCancelledBy(dto.getCancelledBy());
        appointment.setCancelledAt(dto.getCancelledAt());

        return appointment;
    }

    public VitalSigns mapToVitalSigns(VitalSignsDTO dto) {
        if (dto == null) {
            return null;
        }

        VitalSigns vitalSigns = new VitalSigns();
        // ID is auto-generated, don't set it manually

        // Set patient profile relationship (REQUIRED)
        if (dto.getPatientProfileId() != null) {
            PatientProfile patientProfile = patientProfileRepository.findById(dto.getPatientProfileId())
                    .orElseThrow(() -> new ResourceNotFoundException("PatientProfile", "id", dto.getPatientProfileId()));
            vitalSigns.setPatientProfile(patientProfile);
        }

        // Set appointment relationship if ID is provided (OPTIONAL)
        if (dto.getAppointmentId() != null) {
            Appointment appointment = appointmentRepository.findById(dto.getAppointmentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Appointment", "id", dto.getAppointmentId()));
            vitalSigns.setAppointment(appointment);
        }

        vitalSigns.setBloodPressure(dto.getBloodPressure());
        vitalSigns.setHeartRate(dto.getHeartRate());
        vitalSigns.setTemperature(dto.getTemperature());
        vitalSigns.setWeight(dto.getWeight());
        vitalSigns.setHeight(dto.getHeight());
        vitalSigns.setRecordedAt(dto.getRecordedAt());
        vitalSigns.setNotes(dto.getNotes());

        // Note: recordedByRole and recordedByUser are set by the service layer from authentication

        return vitalSigns;
    }
}