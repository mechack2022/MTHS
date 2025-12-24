package com.mths.pharmacy.repository;

import com.mths.pharmacy.entity.Pharmacist;
import com.mths.pharmacy.entity.Pharmacy;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PharmacistRepository extends JpaRepository<Pharmacist, Long> {

    Optional<Pharmacist> findByLicenseNumber(String licenseNumber);

    Optional<Pharmacist> findByRegistrationNumber(String registrationNumber);

    @Query("SELECT p FROM Pharmacist p WHERE p.user.uuid = :userId AND p.deleted = false")
    Optional<Pharmacist> findByUserUuid(@Param("userId") String userId);

    @Query("SELECT p FROM Pharmacist p WHERE p.user.email = :email AND p.deleted = false")
    Optional<Pharmacist> findByUserEmail(@Param("email") String email);

    @Query("SELECT p FROM Pharmacist p WHERE p.pharmacy = :pharmacy AND p.deleted = false")
    List<Pharmacist> findByPharmacy(@Param("pharmacy") Pharmacy pharmacy);

    @Query("SELECT p FROM Pharmacist p WHERE p.pharmacy.id = :pharmacyId AND p.deleted = false")
    List<Pharmacist> findByPharmacyId(@Param("pharmacyId") Long pharmacyId);

    @Query("SELECT p FROM Pharmacist p WHERE p.profileStatus = :status AND p.deleted = false")
    List<Pharmacist> findByProfileStatus(@Param("status") Pharmacist.PharmacistStatus status);

    @Query("SELECT p FROM Pharmacist p WHERE p.isAvailable = true AND p.profileStatus = 'VERIFIED' AND p.deleted = false")
    List<Pharmacist> findAvailablePharmacists();

    @Query("SELECT p FROM Pharmacist p WHERE p.pharmacy.id = :pharmacyId AND p.isAvailable = true AND p.profileStatus = 'VERIFIED' AND p.deleted = false")
    List<Pharmacist> findAvailablePharmacistsByPharmacy(@Param("pharmacyId") Long pharmacyId);

    boolean existsByLicenseNumber(String licenseNumber);

    boolean existsByRegistrationNumber(String registrationNumber);
}
