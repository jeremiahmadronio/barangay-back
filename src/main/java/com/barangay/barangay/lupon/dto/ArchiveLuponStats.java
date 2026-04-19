package com.barangay.barangay.lupon.dto;

public record ArchiveLuponStats(
        Long totalArchived,
        Long archivedThisMonth,
        Long totalArchiveSettled,
        Long totalArchiveCFA
) {
}
