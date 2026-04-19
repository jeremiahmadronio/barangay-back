package com.barangay.barangay.person.dto;

import com.barangay.barangay.enumerated.ResidentStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record UpdateStatusDTO(
        @NotNull(message = "New status is required")
        ResidentStatus status,

        @NotBlank(message = "Reason/Remarks is mandatory for accountability")
        String reason
) {
}
