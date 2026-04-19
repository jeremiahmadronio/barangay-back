package com.barangay.barangay.user_management.dto;

import java.time.LocalDateTime;
import java.util.Set;

public record UserViewDTO(
        String fullName,
        String username,
        String contactNumber,
        String systemEmail,
        Short age,
        String gender,
        String civilStatus,
        String completeAddress,
        String roleName,
        String departments,
        Set<String> permissions,
        String status,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        LocalDateTime lastLoginAt


        ) {
}
