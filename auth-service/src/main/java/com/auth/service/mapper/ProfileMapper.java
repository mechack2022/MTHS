package com.auth.service.mapper;

import com.auth.service.dto.DoctorProfileDTO;
import com.auth.service.dto.PatientProfileDTO;
import com.auth.service.entity.DoctorProfile;
import com.auth.service.entity.PatientProfile;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface ProfileMapper {

    ProfileMapper INSTANCE = Mappers.getMapper(ProfileMapper.class);

    PatientProfileDTO toPatientProfileDTO(PatientProfile profile);
    DoctorProfileDTO toDoctorProfileDTO(DoctorProfile profile);
}
