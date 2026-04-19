package com.barangay.barangay.ftjs.dto;

import java.time.LocalDate;

public record FtjsTableDTO(
        Long id,
        String trackingNumber,
        String fullName,
        Integer issuanceCount,
        String status,
        LocalDate dateSubmitted,
        boolean isResident
) {
}