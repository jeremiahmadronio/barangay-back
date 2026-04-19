package com.barangay.barangay.lupon.dto.dashboard;

import java.time.LocalDateTime;

public record UpcomingHearingDTO (
        Long hearingId,
        String caseTitle,
        String blotterNumber,
        LocalDateTime scheduledStart
) {
}
