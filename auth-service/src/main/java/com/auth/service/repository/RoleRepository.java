package com.auth.service.repository;//package com.digi_dokita.repository;
//
//import com.digi_dokita.constants.AccountType;
//import com.digi_dokita.constants.RoleName;
//import com.digi_dokita.entity.Role;
//import org.springframework.data.jpa.repository.JpaRepository;
//
//import java.util.Optional;
//
//public interface RoleRepository extends JpaRepository<Role, String> {
//    Optional<Role> findByRoleName(RoleName roleName);
//}



import com.auth.service.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.List;
import java.util.Set;

@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {

    Optional<Role> findByRoleName(Role.RoleName roleName);

    boolean existsByRoleName(Role.RoleName roleName);

    @Query("SELECT r FROM Role r JOIN FETCH r.permissions WHERE r.roleName = :roleName")
    Optional<Role> findByRoleNameWithPermissions(@Param("roleName") Role.RoleName roleName);

    @Query("SELECT r FROM Role r JOIN FETCH r.permissions WHERE r.id = :id")
    Optional<Role> findByIdWithPermissions(@Param("id") Long id);

    List<Role> findByIsActive(Boolean isActive);

    @Query("SELECT r FROM Role r JOIN r.permissions p WHERE p.permissionName = :permissionName")
    List<Role> findByPermissionName(@Param("permissionName") com.auth.service.entity.Permission.PermissionName permissionName);

    @Query("SELECT r FROM Role r JOIN FETCH r.permissions")
    List<Role> findAllWithPermissions();

    @Query("SELECT r FROM Role r WHERE r.roleName IN :roleNames")
    Set<Role> findByRoleNameIn(@Param("roleNames") Set<Role.RoleName> roleNames);
}