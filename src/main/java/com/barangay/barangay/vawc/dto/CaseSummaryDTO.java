package com.barangay.barangay.vawc.dto;

import java.time.LocalDateTime;

public record CaseSummaryDTO(

        Long id,
        String caseNumber,
        String victimFullName,
        String violenceTypes,
        String status,
        LocalDateTime dateFiled,
        String assignedOfficer

) {
}
