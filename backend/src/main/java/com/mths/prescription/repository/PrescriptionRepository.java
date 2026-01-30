package com.mths.prescription.repository;

import com.mths.prescription.entity.Prescription;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface PrescriptionRepository extends JpaRepository<Prescription, Long> {

    Optional<Prescription> findByPrescriptionNumber(String prescriptionNumber);

    boolean existsByPrescriptionNumber(String prescriptionNumber);

    @Query("SELECT p FROM Prescription p WHERE p.patientProfile.id = :patientProfileId ORDER BY p.issuedDate DESC")
    List<Prescription> findByPatientProfileId(@Param("patientProfileId") Long patientProfileId);

    @Query("SELECT p FROM Prescription p WHERE p.doctorProfile.id = :doctorProfileId ORDER BY p.issuedDate DESC")
    List<Prescription> findByDoctorProfileId(@Param("doctorProfileId") Long doctorProfileId);

    @Query("SELECT p FROM Prescription p WHERE p.appointment.id = :appointmentId")
    List<Prescription> findByAppointmentId(@Param("appointmentId") Long appointmentId);

    @Query("SELECT p FROM Prescription p WHERE p.patientProfile.id = :patientProfileId AND p.status = 'ACTIVE' ORDER BY p.issuedDate DESC")
    List<Prescription> findActiveByPatientProfileId(@Param("patientProfileId") Long patientProfileId);

    @Query("SELECT p FROM Prescription p WHERE p.pharmacy.id = :pharmacyId ORDER BY p.sentToPharmacyAt DESC")
    List<Prescription> findByPharmacyId(@Param("pharmacyId") Long pharmacyId);

    @Query("SELECT p FROM Prescription p WHERE p.pharmacy.id = :pharmacyId AND p.pharmacyStatus = :status ORDER BY p.sentToPharmacyAt DESC")
    List<Prescription> findByPharmacyIdAndStatus(@Param("pharmacyId") Long pharmacyId, @Param("status") Prescription.PharmacyStatus status);

    @Query("SELECT p FROM Prescription p WHERE p.validUntil < :date AND p.status = 'ACTIVE'")
    List<Prescription> findExpiredPrescriptions(@Param("date") LocalDate date);

    @Query("SELECT p FROM Prescription p WHERE p.patientProfile.id = :patientProfileId AND p.isRefillable = true AND p.refillsRemaining > 0 AND p.status = 'ACTIVE' ORDER BY p.issuedDate DESC")
    List<Prescription> findRefillableByPatientProfileId(@Param("patientProfileId") Long patientProfileId);

    @Query("SELECT p FROM Prescription p WHERE p.creationContext = :context ORDER BY p.issuedDate DESC")
    List<Prescription> findByCreationContext(@Param("context") Prescription.CreationContext context);

    @Query("SELECT COUNT(p) FROM Prescription p WHERE p.doctorProfile.id = :doctorProfileId")
    Long countByDoctorProfileId(@Param("doctorProfileId") Long doctorProfileId);

    @Query("SELECT COUNT(p) FROM Prescription p WHERE p.patientProfile.id = :patientProfileId")
    Long countByPatientProfileId(@Param("patientProfileId") Long patientProfileId);

    @Query("SELECT p FROM Prescription p WHERE p.pharmacyStatus = 'READY_FOR_PICKUP' ORDER BY p.sentToPharmacyAt ASC")
    List<Prescription> findReadyForPickup();

    @Query("SELECT p FROM Prescription p WHERE p.pharmacyStatus = 'PENDING' ORDER BY p.sentToPharmacyAt ASC")
    List<Prescription> findPendingAtPharmacy();
}
