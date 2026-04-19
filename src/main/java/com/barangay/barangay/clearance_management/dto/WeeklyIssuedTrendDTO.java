package com.barangay.barangay.clearance_management.dto;

import java.time.LocalDate;

public record WeeklyIssuedTrendDTO(
        LocalDate date,
        long count
) {
}
