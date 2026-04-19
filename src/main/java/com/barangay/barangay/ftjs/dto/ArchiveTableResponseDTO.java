package com.barangay.barangay.ftjs.dto;

import java.time.LocalDate;

public record ArchiveTableResponseDTO (
        Long id,
        String trackingNumber,
        String fullName,
        Integer issuanceCount,
        String status,
        LocalDate dateSubmitted,
        String archiveRemarks
) {
}
