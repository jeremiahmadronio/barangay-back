package com.barangay.barangay.blotter.dto.reports_and_display;

import com.barangay.barangay.enumerated.TimelineEventType;

import java.time.LocalDateTime;

public record CaseTimeLineDTO (
        Long id,
        TimelineEventType eventType,
        String title,
        String description,
        String performedBy,
        LocalDateTime eventDate
) {
}
