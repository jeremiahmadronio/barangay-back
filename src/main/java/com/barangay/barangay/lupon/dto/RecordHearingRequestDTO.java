package com.barangay.barangay.lupon.dto;

import com.barangay.barangay.enumerated.HearingOutcome;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record RecordHearingRequestDTO (
        @NotBlank String hearingNotes,
        @NotNull HearingOutcome outcome,
        boolean complainantPresent,
        boolean respondentPresent,
        String settlementTerms,
        List<PangkatAttendanceDTO> pangkatAttendance
) {
}
