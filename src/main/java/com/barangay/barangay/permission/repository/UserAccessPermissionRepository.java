package com.barangay.barangay.permission.repository;

import com.barangay.barangay.admin_management.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface UserAccessPermissionRepository extends JpaRepository<User, UUID> {
    @Query("SELECT u FROM User u " +
            "LEFT JOIN FETCH u.role " +
            "LEFT JOIN FETCH u.allowedDepartments " +
            "LEFT JOIN FETCH u.customPermissions " +
            "WHERE u.id = :id")
    Optional<User> findUserWithSecurityDetails(@Param("id") UUID id);
}
