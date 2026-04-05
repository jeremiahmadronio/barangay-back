package com.barangay.barangay.vawc.dto;

public record CategorySummaryDTO(
        String category,
        Long totalCases,
        Long active,
        Long resolved,
        Long pending,
        Double percentage
) {
}
