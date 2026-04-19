package com.barangay.barangay.vawc.dto;

public record DashboardStatsDTO (
        long totalCases,
        double casesTrend,
        long activeBpos,
        long totalSettled,
        double settledTrend,
        long bposIssued,
        double bposTrend
){
}
