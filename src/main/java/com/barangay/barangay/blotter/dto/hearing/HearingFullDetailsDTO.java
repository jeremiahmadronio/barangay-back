package com.barangay.barangay.blotter.dto.hearing;

import com.barangay.barangay.enumerated.HearingOutcome;
import com.barangay.barangay.enumerated.HearingStatus;
import java.time.LocalDateTime;
import java.util.List;

public record HearingFullDetailsDTO(
        Long hearingId,
        Short summonNumber,
        HearingStatus status,
        LocalDateTime scheduledStart,
        String venue,
        String initialNotes,

        MinutesSummaryDTO minutes,

        List<FollowUpSummaryDTO> followUps
) {}



