package com.auth.service.entity;//package com.digi_dokita.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "roles")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = {"users", "permissions"})
public class Role extends BaseEntity {

    @Column(name = "role_name", unique = true, nullable = false)
    @Enumerated(EnumType.STRING)
    private RoleName roleName;

    @Column(name = "description")
    private String description;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    // Many-to-Many relationship with User (inverse side)
    @ManyToMany(mappedBy = "roles", fetch = FetchType.LAZY)
    private Set<User> users = new HashSet<>();

    // Many-to-Many relationship with Permission
    @ManyToMany(fetch = FetchType.EAGER, cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinTable(
            name = "role_permissions",
            joinColumns = @JoinColumn(name = "role_id"),
            inverseJoinColumns = @JoinColumn(name = "permission_id")
    )
    private Set<Permission> permissions = new HashSet<>();

    // Helper methods for managing permissions
    public void addPermission(Permission permission) {
        permissions.add(permission);
        permission.getRoles().add(this);
    }

    public void removePermission(Permission permission) {
        permissions.remove(permission);
        permission.getRoles().remove(this);
    }

    // Constructor for easy role creation
    public Role(RoleName roleName, String description) {
        this.roleName = roleName;
        this.description = description;
    }

    public enum RoleName {
        SUPER_ADMIN,
        ADMIN,
        DOCTOR,
        PATIENT,
        NURSE,
        PHARMACY_ADMIN,
        PHARMACY_STAFF,
        HOSPITAL_ADMIN,
        HOSPITAL_STAFF,
        INSURANCE_ADMIN,
        INSURANCE_AGENT,
        SUPPORT_STAFF,
        MANAGER,
        USER,
        PENDING
    }
}