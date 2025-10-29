package com.mths.auth.repository;

import com.mths.auth.entity.User;
import com.mths.shared.constants.VerificationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.List;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {


     Optional<User> findByEmail(String email);

     boolean existsByEmail(String email);

     @Query("SELECT u FROM User u JOIN FETCH u.roles r JOIN FETCH r.permissions WHERE u.email = :email")
     Optional<User> findByEmailWithRolesAndPermissions(@Param("email") String email);

     @Query("SELECT u FROM User u JOIN FETCH u.roles WHERE u.id = :id")
     Optional<User> findByIdWithRoles(@Param("id") Long id);

     List<User> findByIsActive(Boolean isActive);

     @Query("SELECT u FROM User u JOIN u.roles r WHERE r.roleName = :roleName")
     List<User> findByRoleName(@Param("roleName") User.AccountType roleName);

     @Query("SELECT COUNT(u) FROM User u JOIN u.roles r WHERE r.roleName = :roleName")
     Long countUsersByRole(@Param("roleName") User.AccountType roleName);

     Optional<User> findByUuid(String uuid);

     // New methods for profile validation
     @Query(value = "SELECT CASE WHEN COUNT(*) > 0 THEN true ELSE false END " +
             "FROM patient_profiles pp " +
             "WHERE pp.nin = :nin",
             nativeQuery = true)
     boolean existsByPatientProfileNIN(@Param("nin") String nin);

     @Query(value = "SELECT CASE WHEN COUNT(*) > 0 THEN true ELSE false END " +
             "FROM doctor_profiles dp " +
             "WHERE dp.medical_license_number = :licenseNumber",
             nativeQuery = true)
     boolean existsByDoctorProfileMedicalLicenseNumber(@Param("licenseNumber") String licenseNumber);


     @Query(value = "SELECT CASE WHEN COUNT(*) > 0 THEN true ELSE false END " +
             "FROM lab_technician_profiles ltp " +
             "WHERE ltp.certification_number = :certificationNumber",
             nativeQuery = true)
     boolean existsByLabTechnicianProfileCertificationNumber(@Param("certificationNumber") String certificationNumber);

     @Query(value = "SELECT CASE WHEN COUNT(*) > 0 THEN true ELSE false END FROM (" +
             "SELECT phone_number FROM patient_profiles WHERE phone_number = :phoneNumber " +
             "UNION " +
             "SELECT phone_number FROM doctor_profiles WHERE phone_number = :phoneNumber " +
             "UNION " +
             "SELECT phone_number FROM lab_technician_profiles WHERE phone_number = :phoneNumber" +
             ") AS combined_phones",
             nativeQuery = true)
     boolean existsByProfilePhoneNumber(@Param("phoneNumber") String phoneNumber);

     // Admin verification methods
     Page<User> findByVerificationStatus(VerificationStatus verificationStatus, Pageable pageable);

}
