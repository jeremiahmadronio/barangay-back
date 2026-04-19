package com.barangay.barangay.ftjs.dto;

public record ReportStatsResponseDTO (
        long totalArchive,
        long totalArchiveThisMonth,
        long totalArchiveResident,
        long totalArchiveNonResident
){
}
