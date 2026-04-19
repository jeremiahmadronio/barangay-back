package com.barangay.barangay.vawc.dto;

public record CreateReferralDTO (
        Long caseId,
        String grounds,
        String subjectOfLitigation

) {
}
