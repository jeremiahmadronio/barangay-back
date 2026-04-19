package com.barangay.barangay.blotter.dto.reports_and_display;

public record DocketStatsDTO(
        long totalEntries,
        long activeCases,
        long resolved,
        long pendingMediation
) {
}
