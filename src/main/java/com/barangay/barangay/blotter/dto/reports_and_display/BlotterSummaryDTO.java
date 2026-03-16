package com.barangay.barangay.blotter.dto.reports_and_display;

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