package com.barangay.barangay.users.dto;

import com.barangay.barangay.enumerated.Status;
import jakarta.persistence.SecondaryTable;
import org.springframework.cglib.core.Local;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

public record AdminTable (

        UUID id,
        String username,
        String firstName,
        String lastName,
        String email,
        String contactNumber,
        String roleName,

        Set<String> departments,
        boolean isLocked,
        String status,
        LocalDateTime createdAt,
        LocalDateTime lastLoginAt,
        LocalDateTime lockUntil,
        LocalDateTime updatedAt



) {
}
