package com.barangay.barangay.lupon.dto;

import com.barangay.barangay.admin_management.model.User;
import com.barangay.barangay.enumerated.CaseStatus;
import com.barangay.barangay.enumerated.HearingStatus;

import java.time.LocalDateTime;

public record HearingScheduleDTO(
        Long hearingId,
        String blotterNumber,
        LocalDateTime createdAt,
        String complainantName,
        String respondentName,
        Short summonNumber,
        LocalDateTime scheduledStart,
        LocalDateTime scheduledEnd,
        HearingStatus status,
        String notes,
        String createdBy,
        String venue,
        CaseStatus casePhase,
        Boolean complainantPresent,
        Boolean respondentPresent,
        String hearingNotes,
        String outcome,
        String recordedByMinutes



) {
}
