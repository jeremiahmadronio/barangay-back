package com.barangay.barangay.admin_management.dto;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

public record AdminTable (

        UUID id,
        byte[] photo,
        Set<String> departments,
        String username,
        String firstName,
        String lastName,
        String roleName,
        Set<String> permissions,
        String status,
        LocalDateTime lastLoginAt,
        String systemEmail,
        Short age,
        String contactNumber,
        String gender,
        String completeAddress,
        boolean isLocked,

        LocalDateTime createdAt,
        LocalDateTime lockUntil,
        LocalDateTime updatedAt



) {
}
