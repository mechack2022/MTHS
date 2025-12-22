package com.mths.shared.mapper;

import com.mths.hospital.dto.DoctorProfileDTO;
import com.mths.patient.dto.PatientProfileDTO;
import com.mths.pharmacy.dto.PharmacistProfileDTO;
import com.mths.hospital.entity.DoctorProfile;
import com.mths.patient.entity.PatientProfile;
import com.mths.pharmacy.entity.Pharmacist;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface ProfileMapper {

    ProfileMapper INSTANCE = Mappers.getMapper(ProfileMapper.class);

    PatientProfileDTO toPatientProfileDTO(PatientProfile profile);
    DoctorProfileDTO toDoctorProfileDTO(DoctorProfile profile);

    @Mapping(target = "userId", source = "user.uuid")
    @Mapping(target = "email", source = "user.email")
    @Mapping(target = "fullName", expression = "java(pharmacist.getFullName())")
    @Mapping(target = "pharmacyId", source = "pharmacy.id")
    @Mapping(target = "pharmacyName", source = "pharmacy.pharmacyName")
    @Mapping(target = "age", expression = "java(pharmacist.getAge())")
    @Mapping(target = "isProfileComplete", expression = "java(pharmacist.isProfileComplete())")
    @Mapping(target = "isVerified", expression = "java(pharmacist.isVerified())")
    @Mapping(target = "canDispensePrescriptions", expression = "java(pharmacist.canDispensePrescriptions())")
    @Mapping(target = "isLicenseValid", expression = "java(pharmacist.isLicenseValid())")
    @Mapping(target = "isLicenseExpired", expression = "java(pharmacist.isLicenseExpired())")
    PharmacistProfileDTO toPharmacistProfileDTO(Pharmacist pharmacist);
}
