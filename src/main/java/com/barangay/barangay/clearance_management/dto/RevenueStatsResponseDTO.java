package com.barangay.barangay.clearance_management.dto;

import java.math.BigDecimal;

public record RevenueStatsResponseDTO (
        BigDecimal totalRevenue,
        BigDecimal totalRevenueThisWeek,
        BigDecimal totalRevenueThisMonth,
        BigDecimal totalRevenueThisYear
) {
}
