package com.auth.service.repository;

import com.auth.service.entity.User;
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
//
//     Optional<User> findByVerificationCode(String verificationCode);
//
//     Optional<User> findByPasswordResetCode(String passwordResetCode);

     @Query("SELECT COUNT(u) FROM User u JOIN u.roles r WHERE r.roleName = :roleName")
     Long countUsersByRole(@Param("roleName") User.AccountType roleName);
}
