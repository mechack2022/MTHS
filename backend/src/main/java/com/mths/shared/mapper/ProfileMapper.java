package com.mths.shared.mapper;

import com.mths.hospital.dto.DoctorProfileDTO;
import com.mths.patient.dto.PatientProfileDTO;
import com.mths.hospital.entity.DoctorProfile;
import com.mths.patient.entity.PatientProfile;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface ProfileMapper {

    ProfileMapper INSTANCE = Mappers.getMapper(ProfileMapper.class);

    PatientProfileDTO toPatientProfileDTO(PatientProfile profile);
    DoctorProfileDTO toDoctorProfileDTO(DoctorProfile profile);
}
