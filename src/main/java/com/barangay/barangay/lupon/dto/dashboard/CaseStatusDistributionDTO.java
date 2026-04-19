package com.barangay.barangay.lupon.dto.dashboard;

import com.barangay.barangay.enumerated.CaseStatus;

public record CaseStatusDistributionDTO (
        CaseStatus status,
        Long count
) {
}
