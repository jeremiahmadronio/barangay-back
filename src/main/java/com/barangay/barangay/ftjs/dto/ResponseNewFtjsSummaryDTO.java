package com.barangay.barangay.ftjs.dto;

import java.time.LocalDate;

public record ResponseNewFtjsSummaryDTO(
        Long id,
        LocalDate dateSubmitted,
        Integer issuanceCount,
        String reason
){
}
