package com.auth.service.repository;

import com.auth.service.entity.VitalSigns;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface VitalSignsRepository extends JpaRepository<VitalSigns, Long> {
    
    // Find vital signs by appointment
    Optional<VitalSigns> findByAppointment_Id(Long appointmentId);
    
    // Find vital signs by health status
    List<VitalSigns> findByHealthStatus(VitalSigns.HealthStatus healthStatus);
    
    // Find patient's vital signs history through appointments
    @Query("SELECT v FROM VitalSigns v JOIN v.appointment a WHERE a.patientProfile.id = :patientProfileId ORDER BY v.recordedAt DESC")
    List<VitalSigns> findPatientVitalSignsHistory(@Param("patientProfileId") Long patientProfileId);
    
    // Count vital signs by health status
    @Query("SELECT COUNT(v) FROM VitalSigns v WHERE v.healthStatus = :healthStatus")
    Long countByHealthStatus(@Param("healthStatus") VitalSigns.HealthStatus healthStatus);
}