package com.auth.service.mapper;

import com.auth.service.dto.AppointmentDTO;
import com.auth.service.dto.VitalSignsDTO;
import com.auth.service.entity.Appointment;
import com.auth.service.entity.VitalSigns;
import org.springframework.stereotype.Component;

@Component
public class AppointmentMapper {

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

        // TODO: Add patient and doctor name mapping
        // This would require loading patient and doctor profiles
        // dto.setPatientName(getPatientName(appointment.getPatientProfileId()));
        // dto.setDoctorName(getDoctorName(appointment.getDoctorProfileId()));
        // dto.setDoctorSpecialization(getDoctorSpecialization(appointment.getDoctorProfileId()));

        return dto;
    }

    public VitalSignsDTO mapToVitalSignsDTO(VitalSigns vitalSigns) {
        if (vitalSigns == null) {
            return null;
        }

        VitalSignsDTO dto = new VitalSignsDTO();
        dto.setId(vitalSigns.getId());
        dto.setAppointmentId(vitalSigns.getAppointmentId());
        dto.setBloodPressure(vitalSigns.getBloodPressure());
        dto.setHeartRate(vitalSigns.getHeartRate());
        dto.setTemperature(vitalSigns.getTemperature());
        dto.setWeight(vitalSigns.getWeight());
        dto.setHeight(vitalSigns.getHeight());
        dto.setBmi(vitalSigns.getBmi());
        dto.setRecordedAt(vitalSigns.getRecordedAt());
        dto.setRecordedBy(vitalSigns.getRecordedBy());
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
        appointment.setPatientProfileId(dto.getPatientProfileId());
        appointment.setDoctorProfileId(dto.getDoctorProfileId());
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
        vitalSigns.setAppointmentId(dto.getAppointmentId());
        vitalSigns.setBloodPressure(dto.getBloodPressure());
        vitalSigns.setHeartRate(dto.getHeartRate());
        vitalSigns.setTemperature(dto.getTemperature());
        vitalSigns.setWeight(dto.getWeight());
        vitalSigns.setHeight(dto.getHeight());
        vitalSigns.setRecordedAt(dto.getRecordedAt());
        vitalSigns.setRecordedBy(dto.getRecordedBy());
        vitalSigns.setNotes(dto.getNotes());

        return vitalSigns;
    }
}