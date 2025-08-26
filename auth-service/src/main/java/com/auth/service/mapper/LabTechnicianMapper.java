package com.auth.service.mapper;

import com.auth.service.dto.CreateLabTechnicianProfileRequest;
import com.auth.service.dto.LabTechnicianProfileDTO;
import com.auth.service.dto.UpdateLabTechnicianProfileRequest;
import com.auth.service.entity.LabTechnicianProfile;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface LabTechnicianMapper {

    LabTechnicianMapper INSTANCE = Mappers.getMapper(LabTechnicianMapper.class);

    // Entity to DTO mapping
    @Mapping(source = "user.email", target = "email")
    @Mapping(source = "user.firstName", target = "firstName")
    @Mapping(source = "user.lastName", target = "lastName")
    @Mapping(source = "user.isActive", target = "isActive")
    @Mapping(expression = "java(profile.getFullName())", target = "fullName")
    @Mapping(expression = "java(profile.isProfileComplete())", target = "profileComplete")
    @Mapping(expression = "java(profile.isCertificationValid())", target = "certificationValid")
    LabTechnicianProfileDTO toDTO(LabTechnicianProfile profile);

    // Request to Entity mapping for creation
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "profileType", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    LabTechnicianProfile toEntity(CreateLabTechnicianProfileRequest request);

    // Update entity from request
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "profileType", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    void updateEntityFromRequest(UpdateLabTechnicianProfileRequest request, @MappingTarget LabTechnicianProfile profile);
}