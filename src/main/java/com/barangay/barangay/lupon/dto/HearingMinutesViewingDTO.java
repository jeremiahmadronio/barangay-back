package com.barangay.barangay.lupon.dto;

import com.barangay.barangay.admin_management.model.User;
import com.barangay.barangay.enumerated.HearingStatus;

import java.sql.Time;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public record HearingMinutesViewingDTO (

        Long hearingId,
        Long hearingNumber,
        HearingStatus status,
        LocalDateTime date,
        String venue,
        boolean complinantPresent,
        boolean respondentPresent,
        boolean chairmanPresent,
        boolean secretaryPresent,
        boolean memberPresent,
        String narrative,
        String outcome,
        String recordedBy,
        List<FollowUpSummaryDTO> followUpNotes









) {
}
