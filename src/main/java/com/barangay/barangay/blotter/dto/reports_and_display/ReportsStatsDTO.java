package com.barangay.barangay.blotter.dto.reports_and_display;

public record ReportsStatsDTO(
        long totalEntries, double totalTrend,
        long formalComplaints, double formalTrend,
        long forTheRecord, double recordTrend,
        long referredToLupon, double luponTrend
) {
}
