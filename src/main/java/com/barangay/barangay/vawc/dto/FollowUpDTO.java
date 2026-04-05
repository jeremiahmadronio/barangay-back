package com.barangay.barangay.vawc.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record FollowUpDTO(
        @NotNull(message = "Intervention ID is required.")
        Long interventionId,

        @NotBlank(message = "Follow-up notes cannot be empty.")
        String notes
) {
}
