package com.barangay.barangay.person.dto;

public record ResidentSuggestionsDTO(
        String suggestedBarangayId,
        String suggestedPrecinct,
        String suggestedHouseholdNumber
) {
}
