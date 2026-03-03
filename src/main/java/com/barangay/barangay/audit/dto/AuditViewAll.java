package com.barangay.barangay.audit.dto;

public record AuditViewAll (
        Long id,
        String firstName,
        String lastName,
        String role,
        String ipAddress,
        String module,
        String severity,
        String actionTaken,
        String reason,
        String oldValue,
        String newValue,
        String createdAt,
        String lastLoginAt
) {
}
