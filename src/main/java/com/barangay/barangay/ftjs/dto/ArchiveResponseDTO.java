package com.barangay.barangay.ftjs.dto;

public record ArchiveResponseDTO  (
        long totalArchive,
        long totalArchiveThisMonth,
        long totalArchiveResident,
        long totalArchiveNonResident
){
}
