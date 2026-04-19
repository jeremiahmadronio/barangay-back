package com.barangay.barangay.person.dto;

import jakarta.validation.constraints.*;
import java.time.LocalDate;
import java.util.List;

public record ResidentRegistrationRequestDTO(

        @NotBlank(message = "First name is required")
        @Size(max = 100, message = "First name must not exceed 100 characters")
        String firstName,

        @NotBlank(message = "Last name is required")
        @Size(max = 100, message = "Last name must not exceed 100 characters")
        String lastName,

        @Size(max = 100, message = "Middle name must not exceed 100 characters")
        String middleName,


        @Size(max = 50, message = "Suffix name must not exceed 100 characters")
        String suffix,



        @Size(max = 15, message = "Contact number must not exceed 15 characters")
        String contactNumber,

        @NotBlank(message = "Complete address is required")
        @Size(max = 500, message = "Address is too long")
        String completeAddress,

        @Min(value = 0, message = "Age cannot be negative")
        @Max(value = 150, message = "Invalid age")
        Short age,

        @NotNull(message = "Birth date is required")
        LocalDate birthDate,

        @NotBlank(message = "Gender is required")
        @Size(max = 20)
        String gender,

        @NotBlank(message = "Civil status is required")
        @Size(max = 50)
        String civilStatus,

        @Email(message = "Invalid email format")
        @Size(max = 255)
        String email,

        byte[] photo,

        @NotBlank(message = "Household number is required")
        @Size(max = 25, message = "Household number limit is 25 characters")
        String householdNumber,

        @NotBlank(message = "Precinct number is required")
        @Size(max = 15, message = "Precinct number limit is 15 characters")
        String precinctNumber,

        @NotNull(message = "Voter status is required")
        Boolean isVoter,

        @NotNull(message = "Head of family status is required")
        Boolean isHeadOfFamily,

        @Size(max = 100)
        String occupation,

        @NotBlank(message = "Citizenship is required")
        @Size(max = 50)
        String citizenship,

        @Size(max = 100)
        String religion,

        @Size(max = 20)
        String bloodType,



        @NotBlank(message = "Barangay ID Number is required")
        @Size(max = 30, message = "Barangay ID limit is 30 characters")
        String barangayIdNumber,

        @NotNull(message = "Date of residency is required")
        LocalDate dateOfResidency,


        @NotNull(message = "4Ps status is required")
                Boolean is4ps,

        @NotNull(message = "PWD status is required")
        Boolean isPwd,

        @Size(max = 50)
        String pwdIdNumber,

        @NotNull(message = "Indigency status is required")
        Boolean isIndigent,

        @Size(max = 100)
        String educationalAttainment,

        List<ResidentDocumentRequest>documents


) {}