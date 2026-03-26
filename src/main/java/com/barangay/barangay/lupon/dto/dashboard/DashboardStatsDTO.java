package com.barangay.barangay.lupon.dto.dashboard;

public record DashboardStatsDTO (
        Long hearingsToday,
        Long pendingNewCases,
        Long nearingDeadline,
        Long settledThisMonth

) {
}
