package com.barangay.barangay.clearance_management.dto;

import java.math.BigDecimal;

public record RevenueTrendDTO(
        String label,
        BigDecimal revenue
) {}