package com.barangay.barangay.person.dto;

public record ResidentSummary (
        Long residentId,
        String barangayIdNumber,
        String fullName,
        String contactNumber,
        String householdNumber,
        Boolean isVoter
) {
}
