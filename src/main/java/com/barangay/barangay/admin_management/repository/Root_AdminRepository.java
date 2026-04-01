package com.barangay.barangay.admin_management.repository;

import com.barangay.barangay.enumerated.Status;
import com.barangay.barangay.admin_management.dto.AdminStats;
import com.barangay.barangay.admin_management.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface Root_AdminRepository extends JpaRepository<User, UUID> {

    Optional<User> findByUsername(String username);

    boolean existsByUsername(String username);

    boolean existsBySystemEmail(String email);

    Optional<User> findBySystemEmail(String email);


    boolean existsByUsernameAndIdNot(String username, UUID id);

    //unlock account scheduler
    List<User> findAllByIsLockedTrueAndLockUntilBefore(LocalDateTime now);


    @Query("SELECT COUNT(u) FROM User u JOIN u.role r WHERE r.roleName = 'ADMIN'")
    Long countAllAdmins();

    @Query("SELECT COUNT(u) FROM User u JOIN u.role r WHERE r.roleName = 'ADMIN' AND u.status = :status")
    Long countActiveAdmins(@Param("status") Status status);

    //admin stats
    @Query("""
        SELECT new com.barangay.barangay.admin_management.dto.AdminStats(
            COUNT(u),
            SUM(CASE WHEN u.status = com.barangay.barangay.enumerated.Status.ACTIVE THEN 1 ELSE 0 END),
            SUM(CASE WHEN u.isLocked = true THEN 1 ELSE 0 END),
            SUM(CASE WHEN u.status = com.barangay.barangay.enumerated.Status.INACTIVE THEN 1 ELSE 0 END)
        )
        FROM User u
        JOIN u.role r
        WHERE r.roleName = 'ADMIN'
    """)
    AdminStats getAdminStats();


    // admin table with pagination and filtering
    @Query(value = """
    SELECT DISTINCT u.* FROM users u
    JOIN person p ON p.id = u.person_id
    JOIN roles r ON r.id = u.role_id
    WHERE r.role_name = 'ADMIN'
    AND (CAST(:search AS text) IS NULL OR (
          p.first_name ILIKE CONCAT('%', CAST(:search AS text), '%') OR 
          p.last_name ILIKE CONCAT('%', CAST(:search AS text), '%') OR 
          u.system_email ILIKE CONCAT('%', CAST(:search AS text), '%') OR
          p.email ILIKE CONCAT('%', CAST(:search AS text), '%')
    ))
    AND (CAST(:status AS text) IS NULL OR u.status = :status)
""",
            countQuery = """
    SELECT COUNT(DISTINCT u.id) FROM users u
    JOIN person p ON p.id = u.person_id
    JOIN roles r ON r.id = u.role_id
    WHERE r.role_name = 'ADMIN'
    AND (CAST(:search AS text) IS NULL OR (
          p.first_name ILIKE CONCAT('%', CAST(:search AS text), '%') OR 
          p.last_name ILIKE CONCAT('%', CAST(:search AS text), '%') OR 
          u.system_email ILIKE CONCAT('%', CAST(:search AS text), '%')
    ))
    AND (CAST(:status AS text) IS NULL OR u.status = :status)
""", nativeQuery = true)
    Page<User> findAllAdminsWithFilters(
            @Param("search") String search,
            @Param("status") String status, // Pinadaan natin sa String para sa Postgres compatibility
            Pageable pageable
    );


    @Query("SELECT u FROM User u " +
            "JOIN FETCH u.person " +
            "JOIN FETCH u.role " +
            "LEFT JOIN FETCH u.allowedDepartments " +
            "WHERE u.systemEmail = :email")
    Optional<User> findByEmailWithDepartments(@Param("email") String email);



}
