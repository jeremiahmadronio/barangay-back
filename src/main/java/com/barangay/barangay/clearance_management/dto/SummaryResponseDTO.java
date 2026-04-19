package com.barangay.barangay.clearance_management.dto;

import com.barangay.barangay.enumerated.ClearanceStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record SummaryResponseDTO (
         Long id,
         String certNumber,
         String requestor,
         String certificateTitle,
         BigDecimal fee,
         ClearanceStatus status,
         LocalDateTime requestedAt


) {
}
