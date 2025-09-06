package com.auth.service.repository;

import com.auth.service.entity.DoctorProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DoctorProfileRepository extends JpaRepository<DoctorProfile, Long> {
    
    @Query("SELECT dp FROM DoctorProfile dp JOIN FETCH dp.user WHERE dp.id = :id")
    Optional<DoctorProfile> findByIdWithUser(@Param("id") Long id);
    
    @Query("SELECT dp FROM DoctorProfile dp JOIN FETCH dp.user u WHERE u.id = :userId")
    Optional<DoctorProfile> findByUserId(@Param("userId") Long userId);
    
    Optional<DoctorProfile> findByMedicalLicenseNumber(String medicalLicenseNumber);
    
    boolean existsByMedicalLicenseNumber(String medicalLicenseNumber);
    
    @Query("SELECT dp FROM DoctorProfile dp JOIN FETCH dp.user u WHERE u.isActive = true AND u.accountVerified = true")
    List<DoctorProfile> findAvailableVerifiedDoctors();
}