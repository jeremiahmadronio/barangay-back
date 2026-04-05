package com.barangay.barangay.vawc.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record BpoDetails(
        String caseNumber,
        String complainant,
        String respondent,
        String assignOfficer,
        String bpoNumber,
        LocalDateTime bpoIssuedAt,
        LocalDate bpoExpiredAt

) {
}
