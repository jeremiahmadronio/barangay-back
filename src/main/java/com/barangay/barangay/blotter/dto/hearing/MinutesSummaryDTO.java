package com.barangay.barangay.blotter.dto.hearing;

import com.barangay.barangay.enumerated.HearingOutcome;

public record MinutesSummaryDTO (
        Boolean complainantPresent,
        Boolean respondentPresent,
        String hearingNotes,
        HearingOutcome outcome,
        String recordedBy
) {
}
