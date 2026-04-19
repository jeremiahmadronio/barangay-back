package com.barangay.barangay.blotter.dto.complaint;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public record UpdateCaseDTO(

        Long complainantId,
        Long respondentId,

        String complainantLastName,
        String complainantFirstName,
        String complainantMiddleName,
        String complainantContact,
        Integer complainantAge,
        String complainantGender,
        String complainantCivilStatus,
        String complainantEmail,
        String complainantAddress,

        String respondentLastName,
        String respondentFirstName,
        String respondentMiddleName,
        String respondentAlias,
        Short respondentAge,
        LocalDate respondentDob,
        String respondentGender,
        String respondentCivilStatus,
        String respondentContact,
        String respondentAddress,
        String relationshipTypeName,
        boolean livingWithComplainant,

        @NotNull String natureOfComplaintId,

        @NotNull LocalDate dateOfIncident,
        LocalTime timeOfIncident,
        @NotBlank String placeOfIncident,
        String frequencyOfIncident,
        String descriptionOfInjuries,


        Long assignToId,

        List<String> evidenceTypeIds,

        List<WitnessDTO> witnesses
) {}


