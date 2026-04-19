package com.barangay.barangay.clearance_management.dto;

import java.time.LocalDateTime;

public record RecentRequestResponseDTO(
        String requestorName,
        String certificateType,
        LocalDateTime date,
        String status
) {
}
