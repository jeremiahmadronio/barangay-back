package com.barangay.barangay.blotter.dto.Records;

public record FtrSummaryStatsDTO (
        long totalFtr,
        double ftrTrend,

        long totalEscalated,
        double escalatedTrend,

        double escalationRate,

        String peakIncidentTime,
        long peakTimeCount
){
}
