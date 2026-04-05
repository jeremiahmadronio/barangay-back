package com.barangay.barangay.vawc.dto;

public record ReportStatsDTO (
        Long totalCases,
        Long totalExpired,
        Long resolvedCases,
        Double avgResolutionTime
) {
}
