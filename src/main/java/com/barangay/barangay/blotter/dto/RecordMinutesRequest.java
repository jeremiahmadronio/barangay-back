package com.barangay.barangay.blotter.dto;

import com.barangay.barangay.enumerated.HearingOutcome;
import jakarta.validation.constraints.NotNull;

public record RecordMinutesRequest(
        @NotNull Long hearingId,
        @NotNull Boolean complainantPresent,
        @NotNull Boolean respondentPresent,
        String hearingNotes,
        @NotNull HearingOutcome outcome
) {}