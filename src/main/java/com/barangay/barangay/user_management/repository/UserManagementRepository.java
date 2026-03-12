package com.barangay.barangay.user_management.repository;

import com.barangay.barangay.admin_management.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Repository
public interface UserManagementRepository extends JpaRepository<User, UUID> {
    @Query("""
        SELECT COUNT(DISTINCT u) FROM User u
        JOIN u.allowedDepartments d
        LEFT JOIN u.role r
        WHERE d.id IN :deptIds 
        AND u.id != :currentUserId 
        AND r.roleName NOT IN :excludedRoles
    """)
    long countUsersByDepartments(
            @Param("deptIds") Set<Long> deptIds,
            @Param("currentUserId") UUID currentUserId,
            @Param("excludedRoles") List<String> excludedRoles
    );

    @Query("""
        SELECT COUNT(DISTINCT u) FROM User u
        JOIN u.allowedDepartments d
        LEFT JOIN u.role r
        WHERE d.id IN :deptIds 
        AND u.status = com.barangay.barangay.enumerated.Status.ACTIVE
        AND u.id != :currentUserId 
        AND r.roleName NOT IN :excludedRoles
    """)
    long countActiveUsersByDepartments(
            @Param("deptIds") Set<Long> deptIds,
            @Param("currentUserId") UUID currentUserId,
            @Param("excludedRoles") List<String> excludedRoles
    );

    @Query("""
        SELECT COUNT(DISTINCT u) FROM User u
        JOIN u.allowedDepartments d
        LEFT JOIN u.role r
        WHERE d.id IN :deptIds 
        AND u.status = com.barangay.barangay.enumerated.Status.INACTIVE
        AND u.id != :currentUserId 
        AND r.roleName NOT IN :excludedRoles
    """)
    long countInactiveUsersByDepartments(
            @Param("deptIds") Set<Long> deptIds,
            @Param("currentUserId") UUID currentUserId,
            @Param("excludedRoles") List<String> excludedRoles
    );

    @Query("""
        SELECT COUNT(DISTINCT u) FROM User u
        JOIN u.allowedDepartments d
        LEFT JOIN u.role r
        WHERE d.id IN :deptIds 
        AND u.isLocked = true
        AND u.id != :currentUserId 
        AND r.roleName NOT IN :excludedRoles
    """)
    long countLockedUsersByDepartments(
            @Param("deptIds") Set<Long> deptIds,
            @Param("currentUserId") UUID currentUserId,
            @Param("excludedRoles") List<String> excludedRoles
    );



    @Query("""
    SELECT DISTINCT u FROM User u
    JOIN u.allowedDepartments d
    LEFT JOIN u.role r
    WHERE d.id IN :deptIds
    AND u.id != :currentUserId 
    AND r.roleName NOT IN :excludedRoles 
    AND (:search IS NULL OR 
         LOWER(CAST(u.firstName AS string)) LIKE :search OR 
         LOWER(CAST(u.lastName AS string)) LIKE :search OR 
         LOWER(CAST(u.email AS string)) LIKE :search)
    AND (:roleName IS NULL OR LOWER(r.roleName) = :roleName)
    AND (:deptName IS NULL OR LOWER(d.name) = :deptName)
""")
    Page<User> findStaffByFilters(
            @Param("deptIds") Set<Long> deptIds,
            @Param("currentUserId") UUID currentUserId, // Idinagdag ito
            @Param("excludedRoles") List<String> excludedRoles,
            @Param("search") String search,
            @Param("roleName") String roleName,
            @Param("deptName") String deptName,
            Pageable pageable
    );


    @Query("SELECT u FROM User u LEFT JOIN FETCH u.allowedDepartments WHERE u.id = :id")
    Optional<User> findByIdWithDepartments(@Param("id") UUID id);
}
