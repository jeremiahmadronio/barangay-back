package com.barangay.barangay.blotter.dto.reports_and_display;


public record MediationProcessDTO (
        boolean stepCaseReceived,
        String caseReceivedDate,

        boolean stepSummonIssued,
        String summonStatus,


        boolean stepMediationOngoing,
        int hearingsConducted,

        boolean stepResolved,
        String resolutionStatus


){


}
