package com.barangay.barangay.ftjs.dto;

import java.time.LocalDate;

public record FtjsReportTableDTO(
        Long id,
        String ftjsNumber,
        String fullName,
        String status,
        LocalDate dateSubmitted,
        String contactNumber
) {
}