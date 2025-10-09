package com.auth.service.mapper;

import com.auth.service.dto.VideoConsultationDTO;
import com.auth.service.entity.VideoConsultation;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

@Mapper(componentModel = "spring", uses = {AppointmentMapper.class})
public interface VideoConsultationMapper {

    @Mapping(target = "appointmentId", source = "appointment.id")
    @Mapping(target = "patientName", source = "appointment.patientProfile", qualifiedByName = "getPatientFullName")
    @Mapping(target = "doctorName", source = "appointment.doctorProfile", qualifiedByName = "getDoctorFullName")
    @Mapping(target = "doctorSpecialization", source = "appointment.doctorProfile.specialization")
    VideoConsultationDTO toDTO(VideoConsultation videoConsultation);

    @Mapping(target = "appointment", ignore = true)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    VideoConsultation toEntity(VideoConsultationDTO dto);

    @Named("getPatientFullName")
    default String getPatientFullName(com.auth.service.entity.PatientProfile patientProfile) {
        if (patientProfile == null || patientProfile.getUser() == null) {
            return null;
        }
        return patientProfile.getUser().getFirstName() + " " + patientProfile.getUser().getLastName();
    }

    @Named("getDoctorFullName")  
    default String getDoctorFullName(com.auth.service.entity.DoctorProfile doctorProfile) {
        if (doctorProfile == null || doctorProfile.getUser() == null) {
            return null;
        }
        return "Dr. " + doctorProfile.getUser().getFirstName() + " " + doctorProfile.getUser().getLastName();
    }
}