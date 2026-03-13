package com.barangay.barangay.blotter.dto;

public record DocketStatsDTO(
        long totalEntries,
        long activeCases,
        long resolved,
        long pendingMediation
) {
}
