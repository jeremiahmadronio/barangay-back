package com.barangay.barangay.blotter.dto.complaint;

import com.barangay.barangay.enumerated.CaseStatus;

public record StatusUpdateDTO(
        CaseStatus newStatus,
        String remarks
) {}