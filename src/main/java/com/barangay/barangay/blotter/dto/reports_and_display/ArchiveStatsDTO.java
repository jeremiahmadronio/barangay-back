package com.barangay.barangay.blotter.dto.reports_and_display;

public record ArchiveStatsDTO(
        Long totalArchive,
        Long totalArchiveThisMonth,
        Long totalArchiveFormalComplaint,
        Long totalArchiveForTheRecord
) {
}
