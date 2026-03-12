package com.barangay.barangay.blotter.dto;

import java.time.LocalDate;
import java.time.LocalTime;

public record HearingViewDTO(
        Long hearingId,
        int hearingNumber,
        String status,
        LocalDate date,
        LocalTime startTime,
        LocalTime endTime,
        String venue
) {
}
