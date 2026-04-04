package com.barangay.barangay.vawc.dto;

import com.barangay.barangay.blotter.dto.complaint.WitnessDTO;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public record ComplaintDTO(
        Long complainantId,
        Long respondentId,
        // Section 2: Complainant
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

        @NotBlank String narrativeStatement,

        Long assignToId,



        List<String> evidenceTypeIds,
        List<Long> violenceTypeIds,

        List<WitnessDTO> witnesses
) {}



