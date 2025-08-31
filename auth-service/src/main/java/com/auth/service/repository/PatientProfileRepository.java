package com.auth.service.repository;

import com.auth.service.entity.PatientProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PatientProfileRepository extends JpaRepository<PatientProfile, Long> {
    
    @Query("SELECT pp FROM PatientProfile pp JOIN FETCH pp.user WHERE pp.id = :id")
    Optional<PatientProfile> findByIdWithUser(@Param("id") Long id);
    
    @Query("SELECT pp FROM PatientProfile pp JOIN FETCH pp.user u WHERE u.id = :userId")
    Optional<PatientProfile> findByUserId(@Param("userId") Long userId);
    
    Optional<PatientProfile> findByNIN(String nin);
    
    boolean existsByNIN(String nin);
}