package com.barangay.barangay.lupon.dto;

import jakarta.validation.constraints.NotBlank;

public record UpdateHearingStatusDTO(
        @NotBlank(message = "New status is required (e.g., CANCELLED, RESCHEDULED)")
        String newStatus,

        @NotBlank(message = "Remarks/Reason is required when changing the status")
        String remarks
) {
}
