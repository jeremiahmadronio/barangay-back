package com.barangay.barangay.blotter.dto;

public record BusySlotDTO(
        String startTime,
        String endTime,
        String caseNumber,
        String natureOfComplaint
) {
}
