package com.barangay.barangay.clearance_management.dto;

import com.barangay.barangay.enumerated.ClearanceStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ArchiveSummaryResponseDTO (

        Long id,
        String certNumber,
        String requestor,
        String certTitle,
        BigDecimal fee,
        String status,
        String archiveRemarks

){
}



