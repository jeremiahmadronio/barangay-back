package com.barangay.barangay.person.dto;

import com.barangay.barangay.enumerated.ResidentStatus;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.util.List;

public record ResidentFullProfileViewDTO(

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
        String occupation,
        String barangayIdNumber,
        String householdNumber,
        String precinctNumber,

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

        List<ResidentCaseHistoryDTO> cases,
        List<ResidentDocumentViewDTO> documents

) {
}
