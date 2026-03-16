package com.barangay.barangay.blotter.dto.complaint;

import jakarta.validation.constraints.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public record RecordBlotterEntry(
        @NotBlank String firstName,
        @NotBlank String lastName,
        String middleName,
        @NotBlank String contactNumber,
        @Min(0) Integer age,
        String gender,
        String civilStatus,
        String email,
        @NotBlank String completeAddress,

        @NotBlank String respondentFirstName,
        @NotBlank String respondentLastName,
        String respondentMiddleName,
        String respondentContact,
        String relationshipToComplainant,
        String respondentAddress,

        @NotNull Long natureOfComplaintId,
        @NotNull LocalDate dateOfIncident,
        LocalTime timeOfIncident,
        @NotBlank String placeOfIncident,

        @NotBlank String narrativeStatement,

        List<String>evidenceTypeIds
) {}