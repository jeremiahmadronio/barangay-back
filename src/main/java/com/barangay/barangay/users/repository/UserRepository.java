package com.barangay.barangay.users.repository;

import com.barangay.barangay.enumerated.Status;
import com.barangay.barangay.users.dto.AdminStats;
import com.barangay.barangay.users.model.User;
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
public interface UserRepository extends JpaRepository<User, UUID> {

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
    @Query("SELECT new com.barangay.barangay.users.dto.AdminStats(" +
            "COUNT(u), " +
            "SUM(CASE WHEN u.status = 'ACTIVE' THEN 1 ELSE 0 END)," +
            "SUM(CASE WHEN u.isLocked = true THEN 1 ELSE 0 END), " +
            "SUM(CASE WHEN u.status = 'INACTIVE' THEN 1 ELSE 0 END)) " +
            "FROM User u")
    AdminStats getAdminStats();


    // admin table with pagination and filtering
    @Query("SELECT u FROM User u " +
            "WHERE (:search IS NULL OR " +
            "      LOWER(CAST(u.firstName AS string)) LIKE :search OR " +
            "      LOWER(CAST(u.lastName AS string)) LIKE :search OR " +
            "      LOWER(CAST(u.email AS string)) LIKE :search) " +
            "AND (:roleName IS NULL OR u.role.roleName = :roleName) " +
            "AND (:status IS NULL OR u.status = :status)")
    Page<User> findAllAdminsWithFilters(
            @Param("search") String search,
            @Param("roleName") String roleName,
            @Param("status") Status status,
            Pageable pageable);

}
