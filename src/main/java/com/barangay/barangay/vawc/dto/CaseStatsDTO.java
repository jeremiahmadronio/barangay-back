package com.barangay.barangay.vawc.dto;

public record CaseStatsDTO (
        Long totalCases,
        Long totalClose,
        Long totalExpiringSoon,
        Long totalPending
) {
}
