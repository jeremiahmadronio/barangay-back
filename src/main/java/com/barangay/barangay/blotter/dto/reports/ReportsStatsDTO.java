package com.barangay.barangay.blotter.dto.reports;

public record ReportsStatsDTO(
        long totalEntries, double totalEntriesChange,
        long formalComplaints, double formalChange,
        long forTheRecord, double recordChange,
        long referredToLupon, double luponChange
) {
}
