package com.barangay.barangay.vawc.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;
import java.util.List;

public record InterventionRequestDTO(


        @NotNull(message = "BPO ID is required.")
        Long bpoId,

        @NotBlank(message = "Activity type cannot be empty.")
        String activityType,

        @NotBlank(message = "Intervention details are required.")
        String interventionDetails,

        @NotNull(message = "Intervention date and time are required.")
        LocalDateTime interventionDate,

        @NotNull(message = "Duration is required.")
        @Min(value = 1, message = "Duration must be at least 1 minute.")
        Integer interventionDuration,

        @NotEmpty(message = "At least one employee must be assigned to this intervention.")
        List<Long> performedByEmployeeIds
) {
}
