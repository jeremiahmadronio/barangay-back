package com.barangay.barangay.blotter.dto.hearing;

import java.time.LocalDate;
import java.util.List;

public record MediationHearingViewDTO (

        String hearingTitle,
        String status,
        LocalDate date,
        String timeRange,
        String venue,
        String caseReference,
        String caseSubject,
        String summonTitle,
        List<HearingParticipantDTO> participants
){

}
