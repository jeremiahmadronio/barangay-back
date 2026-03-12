package com.barangay.barangay.blotter.dto;

import java.time.LocalDateTime;

public record CaseNoteViewDTO (
        Long id,
        String note,
        String createdBy,
        LocalDateTime createdAt
) {
}
