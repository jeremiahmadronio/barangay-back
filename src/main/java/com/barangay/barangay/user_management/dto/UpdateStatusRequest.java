package com.barangay.barangay.user_management.dto;

import com.barangay.barangay.enumerated.Status;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record UpdateStatusRequest(
        @NotNull Status newStatus,
        @NotBlank String remarks
) {
}
