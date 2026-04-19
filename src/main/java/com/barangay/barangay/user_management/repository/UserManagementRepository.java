package com.barangay.barangay.user_management.repository;

import com.barangay.barangay.admin_management.model.User;
import com.barangay.barangay.enumerated.Status;
import com.barangay.barangay.person.model.Person;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Repository
public interface UserManagementRepository extends JpaRepository<User, UUID> {

    Optional<User> findByUsername(String username);

    long countByStatus(Status status);
    boolean existsByPerson(Person person);
    boolean existsBySystemEmail(String systemEmail);
    boolean existsByUsername(String username);

        @Query("""
        SELECT COUNT(DISTINCT u) FROM User u 
        JOIN u.allowedDepartments d 
        JOIN u.role r
        WHERE r.roleName NOT IN :excludedDepts
    """)
        Long countUsersExcludingAdminDepts(@Param("excludedDepts") List<String> excludedDepts);



        // 1. Total Global (Excluding Admins AND Archived)
        @Query("""
        SELECT COUNT(u) FROM User u 
        JOIN u.role r 
        WHERE r.roleName NOT IN :excludedRoles 
        AND u.status != com.barangay.barangay.enumerated.Status.ARCHIVED
    """)
        Long countAllGlobal(@Param("excludedRoles") List<String> excludedRoles);


        @Query("""
        SELECT COUNT(u) FROM User u 
        JOIN u.role r 
        WHERE r.roleName NOT IN :excludedRoles 
        AND u.isLocked = true 
        AND u.status != com.barangay.barangay.enumerated.Status.ARCHIVED
    """)
        Long countLockedGlobal(@Param("excludedRoles") List<String> excludedRoles);




    // 2. Active Users (Excluding Admins)
    @Query("SELECT COUNT(u) FROM User u JOIN u.role r WHERE r.roleName NOT IN :excludedRoles AND u.status = 'ACTIVE'")
    Long countActiveGlobal(@Param("excludedRoles") List<String> excludedRoles);

    // 3. Inactive Users (Excluding Admins)
    @Query("SELECT COUNT(u) FROM User u JOIN u.role r WHERE r.roleName NOT IN :excludedRoles AND u.status = 'INACTIVE'")
    Long countInactiveGlobal(@Param("excludedRoles") List<String> excludedRoles);



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
    SELECT u FROM User u 
    LEFT JOIN FETCH u.person 
    LEFT JOIN FETCH u.role 
    LEFT JOIN FETCH u.allowedDepartments 
    LEFT JOIN FETCH u.customPermissions 
    WHERE u.id = :id
""")
    Optional<User> findByIdWithFullDetails(@Param("id") UUID id);

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



        @Query(value = """
        SELECT DISTINCT u FROM User u
        LEFT JOIN u.role r
        JOIN u.person p
        WHERE u.id != :currentUserId
        AND EXISTS (
            SELECT 1 FROM u.allowedDepartments d 
            WHERE d.id IN :deptIds
        )
        AND (r.roleName IS NULL OR r.roleName NOT IN :excludedRoles)
        AND (:search IS NULL OR 
             p.firstName ILIKE :search OR 
             p.lastName ILIKE :search OR 
             u.systemEmail ILIKE :search OR
             u.username ILIKE :search)
        AND (:roleName IS NULL OR r.roleName = :roleName)
        AND (:deptName IS NULL OR EXISTS (
            SELECT 1 FROM u.allowedDepartments d2 WHERE d2.name = :deptName
        ))
    """, countQuery = """
        SELECT COUNT(DISTINCT u) FROM User u
        LEFT JOIN u.role r
        JOIN u.person p
        WHERE u.id != :currentUserId
        AND EXISTS (
            SELECT 1 FROM u.allowedDepartments d 
            WHERE d.id IN :deptIds
        )
        AND (r.roleName IS NULL OR r.roleName NOT IN :excludedRoles)
        AND (:search IS NULL OR 
             p.firstName ILIKE :search OR 
             p.lastName ILIKE :search OR 
             u.systemEmail ILIKE :search OR
             u.username ILIKE :search)
        AND (:roleName IS NULL OR r.roleName = :roleName)
        AND (:deptName IS NULL OR EXISTS (
            SELECT 1 FROM u.allowedDepartments d2 WHERE d2.name = :deptName
        ))
    """)
        Page<User> findStaffByFilters(
                @Param("deptIds") Set<Long> deptIds,
                @Param("currentUserId") UUID currentUserId,
                @Param("excludedRoles") List<String> excludedRoles,
                @Param("search") String search,
                @Param("roleName") String roleName,
                @Param("deptName") String deptName,
                Pageable pageable
        );

        @EntityGraph(attributePaths = {"allowedDepartments"})
        @Query("SELECT u FROM User u WHERE u.id = :id")
        Optional<User> findByIdWithDepartments(@Param("id") UUID id);



    @Query(value = """
    SELECT DISTINCT u FROM User u
    LEFT JOIN u.role r
    JOIN u.person p
    WHERE u.id != :currentUserId
    AND (r.roleName IS NULL OR r.roleName NOT IN :excludedRoles)
    AND (:search IS NULL OR 
         p.firstName ILIKE :search OR 
         p.lastName ILIKE :search OR 
         u.systemEmail ILIKE :search OR
         u.username ILIKE :search)
    AND (:roleName IS NULL OR r.roleName = :roleName)
    AND (:deptName IS NULL OR EXISTS (
        SELECT 1 FROM u.allowedDepartments d2 WHERE d2.name = :deptName
    ))
""", countQuery = """
    SELECT COUNT(DISTINCT u) FROM User u
    LEFT JOIN u.role r
    JOIN u.person p
    WHERE u.id != :currentUserId
    AND (r.roleName IS NULL OR r.roleName NOT IN :excludedRoles)
    AND (:search IS NULL OR 
         p.firstName ILIKE :search OR 
         p.lastName ILIKE :search OR 
         u.systemEmail ILIKE :search OR
         u.username ILIKE :search)
    AND (:roleName IS NULL OR r.roleName = :roleName)
    AND (:deptName IS NULL OR EXISTS (
        SELECT 1 FROM u.allowedDepartments d2 WHERE d2.name = :deptName
    ))
""")
    Page<User> findGlobalStaffByFilters(
            @Param("currentUserId") UUID currentUserId,
            @Param("excludedRoles") List<String> excludedRoles,
            @Param("search") String search,
            @Param("roleName") String roleName,
            @Param("deptName") String deptName,
            Pageable pageable
    );

}
