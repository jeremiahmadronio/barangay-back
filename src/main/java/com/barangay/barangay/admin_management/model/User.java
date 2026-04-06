
package com.barangay.barangay.admin_management.model;

import com.barangay.barangay.department.model.Department;
import com.barangay.barangay.permission.model.Permission;
import com.barangay.barangay.person.model.Person;
import com.barangay.barangay.role.model.Role;
import com.barangay.barangay.enumerated.Status;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;


import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "users")
@Getter  @Setter @AllArgsConstructor @NoArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "person_id", referencedColumnName = "id", unique = true)
    private Person person;

    @Column(unique = true)
    private String username;
    @Column(nullable = false)
    private String password;
    @Column(unique = true, columnDefinition = "TEXT")
    private String systemEmail;
    @Column
    private Integer failedAttempts;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status;
    @Column
    private Boolean isLocked = false;

    @Column
    private LocalDateTime lockUntil;
    @CreationTimestamp
    @Column(updatable = false, nullable = false)
    private LocalDateTime createdAt;
    @UpdateTimestamp
    private LocalDateTime updatedAt;
    @Column
    private LocalDateTime lastLoginAt;


    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "role_id")
    private Role role;

    @Column(name = "mfa_code")
    private String mfaCode;

    @Column(name = "mfa_expiry")
    private LocalDateTime mfaExpiry;

    @Column(name = "is_new_account")
    private boolean isNewAccount = false;


    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "user_departments",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "department_id")
    )
    private Set<Department> allowedDepartments = new HashSet<>();


    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "user_permissions",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "permission_id")
    )
    private Set<Permission> customPermissions = new HashSet<>();














}
