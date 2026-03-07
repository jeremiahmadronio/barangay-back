package com.barangay.barangay.admin_management.dto;

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
