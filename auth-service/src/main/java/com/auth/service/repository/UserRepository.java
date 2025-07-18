package com.auth.service.repository;

import com.auth.service.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, String> {

     Optional<User> findByEmail(String email);

     Boolean existsByEmail(String email);

     Optional<User> findByIdAndEmail(String id, String email);

}
