package com.barangay.barangay.resident.dto;

import java.time.LocalDate;
import java.util.List;

public record ResidentProfileViewDTO (
        Long peopleId,

        String firstName,
        String lastName,
        String middleName,
        String fullName,
        String gender,
        LocalDate birthDate,
        Short age,
        String civilStatus,
        String contactNumber,
        String email,
        String completeAddress,

        String barangayIdNumber,
        String householdNumber,
        String precinctNumber,
        String occupation,
        String citizenship,
        String religion,
        String bloodType,
        Boolean isVoter,
        Boolean isHeadOfFamily,
        LocalDate dateOfResidency,

        List<ResidentCaseHistoryDTO> cases
) {
}
