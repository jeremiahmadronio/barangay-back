package com.barangay.barangay.person.dto;

import java.time.LocalDate;

public record PersonSearchResponseDTO(

        Long id,
        String firstName,
        String lastName,
        String middleName,
        String contactNumber,
        Short age,
        LocalDate birthDate,
        String gender,
        String civilStatus,
        String email,
        String completeAddress,
        Boolean isResident,
        String barangayIdNumber

) {
}
