    package com.barangay.barangay.clearance_management.dto;

    import java.math.BigDecimal;
    import java.time.LocalDate;

    public record DailyCollectionResponseDTO (
            LocalDate date,
            Long totalCertIssue,
            BigDecimal totalCollections,
            String oRNumberStartToEnd   //2026-CL-232323 - 2026-CL-232328
    ) {
    }
