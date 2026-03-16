package com.barangay.barangay.blotter.dto.hearing;

import java.time.LocalDateTime;

public record FollowUpSummaryDTO(
        Long id,
        String remarks,
        String recordedBy,
        LocalDateTime createdAt
) {
}
