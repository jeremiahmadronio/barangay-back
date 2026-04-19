package com.barangay.barangay.lupon.dto.dashboard;

import com.barangay.barangay.enumerated.CaseStatus;
import com.barangay.barangay.enumerated.CaseType;

import java.time.LocalDateTime;

public record RecentCaseDTO(
        Long id,
        String blotterNumber,
        CaseType caseType,
        String complainantName,
        String respondentName,
        CaseStatus status,
        LocalDateTime dateFiled
) {
}
