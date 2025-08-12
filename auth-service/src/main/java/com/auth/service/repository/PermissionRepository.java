package com.auth.service.repository;


import com.auth.service.entity.Permission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.List;
import java.util.Set;

@Repository
public interface PermissionRepository extends JpaRepository<Permission, Long> {

    Optional<Permission> findByPermissionName(Permission.PermissionName permissionName);

    boolean existsByPermissionName(Permission.PermissionName permissionName);

    List<Permission> findByResource(String resource);

    List<Permission> findByAction(String action);

    List<Permission> findByResourceAndAction(String resource, String action);

    List<Permission> findByIsActive(Boolean isActive);

    @Query("SELECT p FROM Permission p JOIN p.roles r WHERE r.roleName = :roleName")
    List<Permission> findByRoleName(@Param("roleName") com.auth.service.entity.Role.RoleName roleName);

    @Query("SELECT p FROM Permission p WHERE p.permissionName IN :permissionNames")
    Set<Permission> findByPermissionNameIn(@Param("permissionNames") Set<Permission.PermissionName> permissionNames);

    @Query("SELECT p FROM Permission p WHERE p.resource = :resource AND p.isActive = true")
    List<Permission> findActivePermissionsByResource(@Param("resource") String resource);
}