package com.barangay.barangay.blotter.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public record FormalComplaintEntry(
        // Section 2: Complainant
        @NotBlank String complainantLastName,
        @NotBlank String complainantFirstName,
        String complainantMiddleName,
        @NotBlank String complainantContact,
        Integer complainantAge,
        String complainantGender,
        String complainantCivilStatus,
        String complainantEmail,
        @NotBlank String complainantAddress,

        // Section 3: Respondent
        @NotBlank String respondentLastName,
        @NotBlank String respondentFirstName,
        String respondentMiddleName,
        String respondentAlias,
        Short respondentAge,
        LocalDate respondentDob,
        String respondentGender,
        String respondentCivilStatus,
        String respondentOccupation,
        String respondentContact,
        String respondentAddress,
        @NotBlank String relationshipTypeName,
        boolean livingWithComplainant,

        // Section 4: Incident Details
        @NotNull Long natureOfComplaintId,

        @NotNull LocalDate dateOfIncident,
        LocalTime timeOfIncident,
        @NotBlank String placeOfIncident,
        String frequencyOfIncident,
        String descriptionOfInjuries,

        @NotBlank String narrativeStatement,

        List<String> evidenceTypeIds,

        List<WitnessDTO> witnesses
) {}

