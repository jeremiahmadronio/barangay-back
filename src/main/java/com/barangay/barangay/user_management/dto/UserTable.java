package com.barangay.barangay.user_management.dto;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

public record UserTable(
        UUID id,
        byte [] photo,
        String username,
        String firstName,
        String lastName,
        String roleName,
        String departmentName,
        Set<String> permissions,
        boolean isLocked,
        String status,
        String statusRemarks,
        LocalDateTime lastLoginAt

) {
}
