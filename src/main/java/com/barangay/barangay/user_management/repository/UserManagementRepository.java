package com.barangay.barangay.user_management.repository;

import com.barangay.barangay.admin_management.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Set;
import java.util.UUID;

@Repository
public interface UserManagementRepository extends JpaRepository<User, UUID> {

    @Query("""
        SELECT COUNT(DISTINCT u) FROM User u
        JOIN u.allowedDepartments d
        WHERE d.id IN :deptIds
    """)
    long countUsersByDepartments(@Param("deptIds") Set<Long> deptIds);

    @Query("""
        SELECT COUNT(DISTINCT u) FROM User u
        JOIN u.allowedDepartments d
        WHERE d.id IN :deptIds AND u.status = com.barangay.barangay.enumerated.Status.ACTIVE
    """)
    long countActiveUsersByDepartments(@Param("deptIds") Set<Long> deptIds);

    @Query("""
        SELECT COUNT(DISTINCT u) FROM User u
        JOIN u.allowedDepartments d
        WHERE d.id IN :deptIds AND u.status = com.barangay.barangay.enumerated.Status.INACTIVE
    """)
    long countInactiveUsersByDepartments(@Param("deptIds") Set<Long> deptIds);

    @Query("""
        SELECT COUNT(DISTINCT u) FROM User u
        JOIN u.allowedDepartments d
        WHERE d.id IN :deptIds AND u.isLocked = true
    """)
    long countLockedUsersByDepartments(@Param("deptIds") Set<Long> deptIds);
}
