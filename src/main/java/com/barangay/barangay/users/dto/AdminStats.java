package com.barangay.barangay.users.dto;

public record AdminStats(
        Long totalAdmin,
        Long totalActive,
        Long totalLock,
        Long totalInactive
) {
}
