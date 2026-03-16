package com.barangay.barangay.blotter.dto.hearing;

public record BusySlotDTO(
        String startTime,
        String endTime,
        String caseNumber,
        String natureOfComplaint
) {
}
