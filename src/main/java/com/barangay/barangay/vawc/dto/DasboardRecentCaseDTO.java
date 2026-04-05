package com.barangay.barangay.vawc.dto;

public record DasboardRecentCaseDTO (
        String caseNumber,
        String complainantName,
        String natureOfComplaint,
        String status
) {
}
