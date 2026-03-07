package com.barangay.barangay.admin_management.dto;

public record AdminStats(
        Long totalAdmin,
        Long totalActive,
        Long totalLock,
        Long totalInactive
) {
}
