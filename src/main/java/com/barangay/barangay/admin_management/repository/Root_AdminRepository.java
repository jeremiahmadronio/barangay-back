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
    boolean existsByEmail(String email);

    Optional<User> findByEmail(String email);

    boolean existsByEmailAndIdNot(String email, UUID id);
    boolean existsByUsernameAndIdNot(String username, UUID id);

    //unlock account scheduler
    List<User> findAllByIsLockedTrueAndLockUntilBefore(LocalDateTime now);


    @Query("SELECT COUNT(u) FROM User u")
    Long countAllUsers();

    @Query("SELECT COUNT(u) FROM User u WHERE u.status = com.barangay.barangay.enumerated.Status.ACTIVE")
    Long countActiveUsers();

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
    @Query("""
    SELECT DISTINCT u FROM User u
    JOIN u.role r
    WHERE r.roleName = 'ADMIN'
    AND (:search IS NULL OR (
          u.firstName ILIKE :search OR 
          u.lastName ILIKE :search OR 
          u.email ILIKE :search
    ))
    AND (:status IS NULL OR u.status = :status)
""")
    Page<User> findAllAdminsWithFilters(
            @Param("search") String search,
            @Param("status") Status status,
            Pageable pageable);

}
