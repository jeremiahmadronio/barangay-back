package com.barangay.barangay.user_management.dto;

import java.util.UUID;

public record UserStats(
        UUID id,
        Long totalUser,
        Long totalActiveUser,
        Long totalInactive,
        Long totalLock

) {
}
