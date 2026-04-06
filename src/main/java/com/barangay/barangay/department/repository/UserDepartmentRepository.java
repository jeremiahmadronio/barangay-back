package com.barangay.barangay.department.repository;

import com.barangay.barangay.admin_management.model.User;
import com.barangay.barangay.department.model.Department;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserDepartmentRepository extends JpaRepository<User, UUID> {

    @Query("SELECT u FROM User u LEFT JOIN FETCH u.allowedDepartments WHERE u.systemEmail = :email")
    Optional<User> findByEmailWithDepartments(@Param("email") String email);





    @Query("SELECT u FROM User u " +
            "LEFT JOIN FETCH u.allowedDepartments " +
            "WHERE u.id = :id")
    Optional<User> findByIdWithDepartments(@Param("id") UUID id);

    @Query("SELECT u FROM User u " +
            "LEFT JOIN FETCH u.allowedDepartments " +
            "WHERE u.username = :username")
    Optional<User> findByUsernameWithDepartments(@Param("username") String username);
}
