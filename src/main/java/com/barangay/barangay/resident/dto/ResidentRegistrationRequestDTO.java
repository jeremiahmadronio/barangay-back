package com.barangay.barangay.resident.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

public record ResidentRegistrationRequestDTO(
        @NotBlank(message = "First name is required")
        String firstName,

        @NotBlank(message = "Last name is required")
        String lastName,

        String middleName,

        @Size(max = 15)
        String contactNumber,

        String completeAddress,

        Short age,
        LocalDate birthDate,

        String gender,
        String civilStatus,
        String email,
        byte[] photo,

        // --- Resident Specific Info ---
        String householdNumber,
        String precinctNumber,

        @NotNull
        Boolean isVoter,

        @NotNull
        Boolean isHeadOfFamily,

        String occupation,
        String citizenship,
        String religion,
        String bloodType,

            @NotBlank(message = "Barangay ID Number is required")
        String barangayIdNumber,

        LocalDate dateOfResidency
) {}