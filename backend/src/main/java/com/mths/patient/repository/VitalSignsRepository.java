package com.mths.patient.repository;

import com.mths.patient.entity.VitalSigns;
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

    // Find vital signs by patient profile directly (both standalone and appointment-linked)
    @Query("SELECT v FROM VitalSigns v WHERE v.patientProfile.id = :patientProfileId ORDER BY v.recordedAt DESC")
    List<VitalSigns> findByPatientProfileId(@Param("patientProfileId") Long patientProfileId);

    // Find only standalone vital signs (not linked to appointments)
    @Query("SELECT v FROM VitalSigns v WHERE v.patientProfile.id = :patientProfileId AND v.appointment IS NULL ORDER BY v.recordedAt DESC")
    List<VitalSigns> findStandaloneVitalSignsByPatientId(@Param("patientProfileId") Long patientProfileId);

    // Find vital signs by health status
    List<VitalSigns> findByHealthStatus(VitalSigns.HealthStatus healthStatus);

    // Find patient's vital signs history through appointments (DEPRECATED - use findByPatientProfileId)
    @Query("SELECT v FROM VitalSigns v JOIN v.appointment a WHERE a.patientProfile.id = :patientProfileId ORDER BY v.recordedAt DESC")
    List<VitalSigns> findPatientVitalSignsHistory(@Param("patientProfileId") Long patientProfileId);

    // Find vital signs with severe health status
    @Query("SELECT v FROM VitalSigns v WHERE v.healthStatus = 'SEVERE' ORDER BY v.recordedAt DESC")
    List<VitalSigns> findSevereVitalSigns();

    // Count vital signs by health status
    @Query("SELECT COUNT(v) FROM VitalSigns v WHERE v.healthStatus = :healthStatus")
    Long countByHealthStatus(@Param("healthStatus") VitalSigns.HealthStatus healthStatus);
}