package com.barangay.barangay.ftjs.dto;

import com.barangay.barangay.enumerated.TimeLineType;

import java.time.LocalDateTime;

public record TimelineResponseDTO (
        Long id,
        String title,
        String description,
        TimeLineType type,
        LocalDateTime eventDate,
        String createdBy
) {
}
