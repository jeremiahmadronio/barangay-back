package com.barangay.barangay.ftjs.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record ResponseNewFtjsFullDetailsDTO (
        Long id,
        String residentFullName,
        String reason,
        LocalDate dateOfLoss,
        Integer issuanceNumber,
        BigDecimal amountPaid,
        String orNumber,
        String createdBy,
        LocalDateTime createdAt,
        byte[] fileAttach

) {
}
