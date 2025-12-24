package com.mths.pharmacy.repository;

import com.mths.pharmacy.entity.Pharmacy;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PharmacyRepository extends JpaRepository<Pharmacy, Long> {

    Optional<Pharmacy> findByLicenseNumber(String licenseNumber);

    Optional<Pharmacy> findByRegistrationNumber(String registrationNumber);

    @Query("SELECT p FROM Pharmacy p WHERE p.status = :status AND p.deleted = false")
    List<Pharmacy> findByStatus(@Param("status") Pharmacy.PharmacyStatus status);

    @Query("SELECT p FROM Pharmacy p WHERE p.isActive = true AND p.status = 'VERIFIED' AND p.deleted = false")
    List<Pharmacy> findActivePharmacies();

    @Query("SELECT p FROM Pharmacy p WHERE p.city = :city AND p.isActive = true AND p.status = 'VERIFIED' AND p.deleted = false")
    List<Pharmacy> findActivePharmaciesByCity(@Param("city") String city);

    @Query("SELECT p FROM Pharmacy p WHERE p.state = :state AND p.isActive = true AND p.status = 'VERIFIED' AND p.deleted = false")
    List<Pharmacy> findActivePharmaciesByState(@Param("state") String state);

    boolean existsByLicenseNumber(String licenseNumber);

    boolean existsByRegistrationNumber(String registrationNumber);

    boolean existsByEmail(String email);
}
