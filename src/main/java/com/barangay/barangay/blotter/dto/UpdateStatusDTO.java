package com.barangay.barangay.blotter.dto;

import com.barangay.barangay.enumerated.CaseStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record UpdateStatusDTO(
        @NotNull(message = "Case ID is required")
        Long caseId,

        @NotNull(message = "New status is required")
        CaseStatus newStatus,

        @NotBlank(message = "Reason/Remarks is required for status changes")
        String reason
) {}