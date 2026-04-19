package com.barangay.barangay.ftjs.dto.dashboard;

public record DashboardStatsResponseDTO (

        long totalIssueToday,
        long totalIssueLastWeek,
        long totalArchiveThisWeek,
        long totalNonResidentIssueThisWeek

) {
}