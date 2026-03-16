package com.barangay.barangay.blotter.dto.reports_and_display;

import com.barangay.barangay.blotter.dto.complaint.WitnessDTO;
import com.barangay.barangay.enumerated.CaseStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

public record BlotterDocketViewDTO (


        LocalDate mediationDeadline,
        long daysRemaining,

        String caseNumber,
        CaseStatus caseStatus,
        String caseStatusRemarks,
        LocalDateTime dateFiled,


        String firstName,
        String lastName,
        String middleName,
        String contactNumber,
        Short age,
        String gender,
        String civilStatus,
        String email,
        String completeAddress,

        String respondentFirstName,
        String respondentLastName,
        String respondentMiddleName,
        String respondentAlias,
        String respondentContact,
        Integer respondentAge,
        String respondentGender,
        LocalDate respondentDateOfBirth,
        String respondentCivilStatus,
        String respondentOccupation,
        String relationshipToComplainant,
        String respondentAddress,
        boolean livingWithComplainant,

        String natureOfComplaint,
        LocalDate incidentDate,
        LocalTime incidentTime,
        String incidentLocation,
        String frequencyOfIncident,
        String descriptionOfInjuries,

        String narrative,
        List<String> evidenceTypeIds,
        List<WitnessDTO> witnesses
){


}
