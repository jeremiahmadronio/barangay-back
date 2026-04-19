package com.barangay.barangay.blotter.dto.hearing;

import jakarta.validation.constraints.NotBlank;

public record FollowUpHearingDTO(
        @NotBlank
        String notes
) {
}
