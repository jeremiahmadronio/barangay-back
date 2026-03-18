package com.barangay.barangay.lupon.dto;

import java.time.LocalDateTime;

public record LuponSummaryDTO (
        Long id,
        String blotterNumber,
        String complainantName,
        String respondentName,
        String natureOfComplaint,
        LocalDateTime dateFiled,
        String status
){
}
