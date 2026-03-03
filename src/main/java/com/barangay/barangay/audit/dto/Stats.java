package com.barangay.barangay.audit.dto;

public record Stats(
        Long todayEntry,
        Long totalEntries,
        Long totalWarning,
        Long totalCritical
) {
}
