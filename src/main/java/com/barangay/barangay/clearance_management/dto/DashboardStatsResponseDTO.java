package com.barangay.barangay.clearance_management.dto;


import java.math.BigDecimal;

public record DashboardStatsResponseDTO (
        long totalIssuedToday,
        BigDecimal revenueToday,
        long totalArchiveToday,
        long totalFreeCertsReleaseToday
) {
}
