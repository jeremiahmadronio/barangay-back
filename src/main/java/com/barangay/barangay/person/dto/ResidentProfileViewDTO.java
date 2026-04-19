package com.barangay.barangay.person.dto;

import com.barangay.barangay.enumerated.ResidentStatus;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.util.List;

public record ResidentProfileViewDTO (
        Long peopleId,
        byte[] photo,
        String firstName,
        String lastName,
        String middleName,
        String suffix,
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
        Boolean is4ps,
        Boolean isPwd,
        String pwdIdNumber,
        Boolean isIndigent,
        String educationalAttainment,
        ResidentStatus status,



        List<ResidentCaseHistoryDTO> cases
) {
}
