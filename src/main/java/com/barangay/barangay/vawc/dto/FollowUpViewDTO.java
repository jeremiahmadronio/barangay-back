package com.barangay.barangay.vawc.dto;

import java.time.LocalDateTime;

public record FollowUpViewDTO(
        Long id,
        String notes,
        LocalDateTime createdAt,
        String createdBy
) {
}
