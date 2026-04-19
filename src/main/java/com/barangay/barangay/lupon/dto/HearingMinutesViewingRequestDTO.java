package com.barangay.barangay.lupon.dto;

import java.util.List;

public record HearingMinutesViewingRequestDTO(
        Long hearingId,
        Short hearingNumber, // Ito 'yung summonNumber mo
        String status,
        String date,
        String startTime,
        String endTime,
        String venue,
        String caseNumber,
        String caseTitle,
        List<AssignedPangkatDTO> assignedPangkat
) {
}
