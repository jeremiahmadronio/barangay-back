package com.barangay.barangay.person.dto;

import jakarta.validation.constraints.*;
import java.time.LocalDate;
import java.util.List;

public record UpdateResidentProfileDTO(

        @Size(max = 100, message = "First name must not exceed 100 characters")
        String firstName,

        @Size(max = 100, message = "Last name must not exceed 100 characters")
        String lastName,

        @Size(max = 100, message = "Middle name must not exceed 100 characters")
        String middleName,

        @Size(max = 50, message = "Suffix name must not exceed 100 characters")
        String suffix,

        @Size(max = 15, message = "Contact number must not exceed 15 characters")
        String contactNumber,

        @Size(max = 500, message = "Address is too long")
        String completeAddress,

        @Min(value = 0, message = "Age cannot be negative")
        @Max(value = 150, message = "Invalid age")
        Short age,

        LocalDate birthDate,

        @Size(max = 20)
        String gender,

        @Size(max = 50)
        String civilStatus,

        @Email(message = "Invalid email format")
        @Size(max = 255)
        String email,

        byte[] photo,

        @Size(max = 25, message = "Household number limit is 25 characters")
        String householdNumber,

        @Size(max = 15, message = "Precinct number limit is 15 characters")
        String precinctNumber,

        Boolean isVoter,

        Boolean isHeadOfFamily,

        @Size(max = 100)
        String occupation,

        @Size(max = 50)
        String citizenship,

        @Size(max = 100)
        String religion,

        @Size(max = 20)
        String bloodType,

        Boolean is4ps,
        Boolean isPwd,
        @Size(max = 50)
        String pwdIdNumber,
        Boolean isIndigent,
        @Size(max = 100)
        String educationalAttainment,

        @Size(max = 30, message = "Barangay ID limit is 30 characters")
        String barangayIdNumber,

        LocalDate dateOfResidency,
        List<UpdateDocumentRequest> documents
) {
}