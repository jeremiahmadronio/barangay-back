package com.barangay.barangay.lupon.dto.reports;

public record ReportsStatsDTO(
        Long escalate,
        Long totalSettled,
        Long totalClosed,
        Long totalCFA
) {
}
