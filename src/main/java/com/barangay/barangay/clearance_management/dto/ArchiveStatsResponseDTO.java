package com.barangay.barangay.clearance_management.dto;

import java.math.BigDecimal;

public record ArchiveStatsResponseDTO (
        long totalArchiveIssued,
        BigDecimal lostRevenue,
        long totalArchiveTemplate,
        String mostArchiveTemplate
) {
}
