package com.barangay.barangay.user_management.dto;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

public record UserTable(
        UUID id,
        String username,
        String firstName,
        String lastName,
        String email,
        String contactNumber,
        String roleName,

        String departmentName,
        Set<String> permissions,
        boolean isLocked,
        String status,
        LocalDateTime createdAt,
        LocalDateTime lastLoginAt,
        LocalDateTime lockUntil,
        LocalDateTime updatedAt
) {
}
