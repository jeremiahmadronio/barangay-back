package com.barangay.barangay.clearance_management.dto;

import java.math.BigDecimal;

public record RevenueResponseByCertificate(
        String certificateTitle,
        Long count,
        BigDecimal fee,
        Object totalRevenue
) {

}