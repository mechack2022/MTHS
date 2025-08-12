package com.auth.service.entity;//package com.digi_dokita.entity;
//import com.digi_dokita.constants.RoleName;
//import jakarta.persistence.Entity;
//import jakarta.persistence.Table;
//import lombok.Builder;
//import lombok.Getter;
//import lombok.Setter;
//import jakarta.persistence.*;
//import lombok.*;
//
//import java.time.LocalDateTime;
//
//@Entity
//@Table(name = "roles")
//@Setter
//@Getter
//@Builder
//@NoArgsConstructor
//@AllArgsConstructor
//public class Role {
//
//    @Id
//    @GeneratedValue(strategy = GenerationType.IDENTITY) // or AUTO
//    private Long id;
//
//    @Enumerated(EnumType.STRING)
//    @Column(unique = true, nullable = false)
//    private RoleName roleName;
//
//    private String description;
//
//    @Column(name = "created_at")
//    private LocalDateTime createdAt;
//}

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

//    @Column(name="uuid", nullable = false, unique = true)
//    private String uuid;

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
        USER
    }
}