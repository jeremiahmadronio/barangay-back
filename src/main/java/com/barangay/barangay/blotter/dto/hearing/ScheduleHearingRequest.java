package com.barangay.barangay.blotter.dto.hearing;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

public record ScheduleHearingRequest(
        @NotBlank(message = "Blotter number is required") String blotterNumber,
        @NotNull(message = "Start time is required") LocalDateTime scheduledStart,
        @NotNull(message = "End time is required") LocalDateTime scheduledEnd,
        @NotBlank(message = "Venue is required") String venue,
        String notes
) {}