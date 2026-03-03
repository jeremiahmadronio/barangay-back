package com.barangay.barangay.users.repository;

import com.barangay.barangay.users.dto.AdminStats;
import com.barangay.barangay.users.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {

    Optional<User> findByUsername(String username);

    boolean existsByUsername(String username);
    boolean existsByEmail(String email);


    @Query("SELECT new com.barangay.barangay.users.dto.AdminStats(" +
            "COUNT(u), " +
            "SUM(CASE WHEN u.status = 'ACTIVE' THEN 1 ELSE 0 END)," +
            "SUM(CASE WHEN u.isLocked = true THEN 1 ELSE 0 END), " +
            "SUM(CASE WHEN u.status = 'INACTIVE' THEN 1 ELSE 0 END)) " +
            "FROM User u")
    AdminStats getAdminStats();


}
