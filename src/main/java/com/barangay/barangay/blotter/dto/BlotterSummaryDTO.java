package com.barangay.barangay.blotter.dto;

import java.time.LocalDateTime;

public record BlotterSummaryDTO(
        Long id,
        String blotterNumber,
        String complainantName,
        String respondentName,
        String natureOfComplaint,
        LocalDateTime dateFiled,
        String status
) {}