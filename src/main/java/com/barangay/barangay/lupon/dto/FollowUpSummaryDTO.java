package com.barangay.barangay.lupon.dto;

import java.time.LocalDateTime;

public record FollowUpSummaryDTO (
        Long id,
        String remarks,
        String recordedBy,
        LocalDateTime createdAt
){
}
