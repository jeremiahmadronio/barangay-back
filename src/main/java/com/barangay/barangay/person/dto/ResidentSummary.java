package com.barangay.barangay.person.dto;

import com.barangay.barangay.enumerated.ResidentStatus;

public record ResidentSummary (
        Long residentId,
        String barangayIdNumber,
        byte[] photo,
        String fullName,
        String contactNumber,
        String householdNumber,
        Boolean isVoter,
        ResidentStatus status

) {
}
