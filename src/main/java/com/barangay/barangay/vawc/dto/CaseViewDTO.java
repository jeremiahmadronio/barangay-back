package com.barangay.barangay.vawc.dto;

import com.barangay.barangay.blotter.dto.complaint.WitnessDTO;
import com.barangay.barangay.blotter.dto.reports_and_display.CaseHandleByDTO;
import com.barangay.barangay.enumerated.CaseStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

public record CaseViewDTO (

        LocalDate bpoDeadline,
        String remainingTime,

        String caseNumber,
        CaseStatus caseStatus,
        String caseStatusRemarks,
        LocalDateTime dateFiled,
        String assignOfficer,
        String caseFiledBy,

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
        List<String> evidenceNames,
        List<WitnessDTO> witnesses,
        List<ViolenceTypeDTO> violenceTypes




) {
}
