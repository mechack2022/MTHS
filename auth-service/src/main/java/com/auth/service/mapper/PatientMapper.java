//package com.auth.service.mapper;
//
//import com.auth.service.dto.PatientCreateDto;
//import com.auth.service.dto.PatientDto;
//import com.auth.service.dto.PatientUpdateDto;
//import com.auth.service.entity.Patient;
//import org.mapstruct.*;
//
//import java.util.List;
//
//@Mapper(componentModel = "spring",
//        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
//        nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS)
//public interface PatientMapper {
//
//    // Entity to DTO mapping - flattening user fields
//    @Mapping(source = "user.id", target = "userId")
//    @Mapping(source = "user.uuid", target = "userUuid")
//    @Mapping(source = "user.email", target = "email")
//    @Mapping(source = "user.firstName", target = "firstName")
//    @Mapping(source = "user.lastName", target = "lastName")
//    @Mapping(source = "user.mailVerified", target = "mailVerified")
//    @Mapping(source = "user.accountType", target = "accountType")
//    @Mapping(source = "user.isActive", target = "isActive")
//    @Mapping(source = "user.accountVerified", target = "accountVerified")
//    @Mapping(source = "user.roles", target = "roles", ignore = true) // Handle separately for security
//    PatientDto toDto(Patient patient);
//
//    // Entity to DTO list mapping
//    List<PatientDto> toDtoList(List<Patient> patients);
//
//    // Create DTO to Patient entity (user relationship handled in service)
//    @Mapping(target = "id", ignore = true)
//    @Mapping(target = "user", ignore = true)
//    @Mapping(target = "createdAt", ignore = true)
//    @Mapping(target = "updatedAt", ignore = true)
//    @Mapping(target = "createdBy", ignore = true)
//    @Mapping(target = "updatedBy", ignore = true)
//    @Mapping(target = "version", ignore = true)
//    @Mapping(target = "deleted", constant = "false")
//    Patient toEntity(PatientCreateDto createDto);
//
//    // Update existing patient entity from DTO (excluding user fields)
//    @Mapping(target = "id", ignore = true)
//    @Mapping(target = "user", ignore = true)
//    @Mapping(target = "createdAt", ignore = true)
//    @Mapping(target = "updatedAt", ignore = true)
//    @Mapping(target = "createdBy", ignore = true)
//    @Mapping(target = "updatedBy", ignore = true)
//    @Mapping(target = "version", ignore = true)
//    @Mapping(target = "deleted", ignore = true)
//    void updateEntityFromDto(PatientUpdateDto updateDto, @MappingTarget Patient patient);
//
//    // Basic DTO mapping without sensitive information
//    @Mapping(source = "user.uuid", target = "userUuid")
//    @Mapping(source = "user.email", target = "email")
//    @Mapping(source = "user.firstName", target = "firstName")
//    @Mapping(source = "user.lastName", target = "lastName")
//    @Mapping(source = "user.isActive", target = "isActive")
//    @Mapping(target = "roles", ignore = true)
//    @Mapping(target = "NIN", ignore = true)
//    @Mapping(target = "userId", ignore = true)
//    PatientDto toBasicDto(Patient patient);
//
//    // Mapping with roles included (for authenticated users with proper permissions)
//    @Named("withRoles")
//    @Mapping(source = "user.id", target = "userId")
//    @Mapping(source = "user.uuid", target = "userUuid")
//    @Mapping(source = "user.email", target = "email")
//    @Mapping(source = "user.firstName", target = "firstName")
//    @Mapping(source = "user.lastName", target = "lastName")
//    @Mapping(source = "user.mailVerified", target = "mailVerified")
//    @Mapping(source = "user.accountType", target = "accountType")
//    @Mapping(source = "user.isActive", target = "isActive")
//    @Mapping(source = "user.accountVerified", target = "accountVerified")
//    @Mapping(source = "user.roles", target = "roles")
//    PatientDto toDtoWithRoles(Patient patient);
//
//    // Profile-only update (medical/personal info only, no user info)
//    @Mapping(target = "id", ignore = true)
//    @Mapping(target = "user", ignore = true)
//    @Mapping(target = "createdAt", ignore = true)
//    @Mapping(target = "updatedAt", ignore = true)
//    @Mapping(target = "createdBy", ignore = true)
//    @Mapping(target = "updatedBy", ignore = true)
//    @Mapping(target = "version", ignore = true)
//    @Mapping(target = "deleted", ignore = true)
//    @Mapping(target = "NIN", ignore = true)
//    void updateProfileFromDto(PatientUpdateDto updateDto, @MappingTarget Patient patient);
//}