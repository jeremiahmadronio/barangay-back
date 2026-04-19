package com.barangay.barangay.audit.dto;

public record AuditTable (
        Long id,
        String firstName,
        String lastName,
        String roleName,
        String actionTaken,
        String module,
        String reason,
        String ipAddress,
        String severity

) {
}
