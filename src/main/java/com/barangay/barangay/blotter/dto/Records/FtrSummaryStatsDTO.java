package com.barangay.barangay.blotter.dto.Records;

public record FtrSummaryStatsDTO (
        long totalFtr,
        double ftrTrend,

        long frequentSubjectsCount,
        String mostReportedIssue,

        String peakIncidentTime,
        long peakTimeCount
){
}