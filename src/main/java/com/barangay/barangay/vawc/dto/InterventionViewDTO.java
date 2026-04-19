package com.barangay.barangay.vawc.dto;

import java.time.LocalDateTime;
import java.util.List;

public record InterventionViewDTO(
        Long id,
        String activityType,
        String details,
        LocalDateTime interventionDate,
        Integer duration,
        String createdBy,
        List<String> performedBy,
        List<FollowUpViewDTO> followUps
) {
}
