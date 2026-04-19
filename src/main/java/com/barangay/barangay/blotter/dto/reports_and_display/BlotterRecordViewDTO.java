package com.barangay.barangay.blotter.dto.reports_and_display;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

public record BlotterRecordViewDTO(
        // System Metadata
        Long id,
        String blotterNumber,
        LocalDateTime dateFiled,
        String status,
        String encodedBy,

        // Complainant Details (Mapped from People)
        String complainantFullName,
        String complainantContact,
        String complainantAddress,
        String civilStatus,
        Integer complainantAge,
        String complainantGender,
        String complainantEmail,

        // Respondent Details
        String respondentFullName,
        String respondentContact,
        String relationshipToComplainant,
        String respondentAddress,

        // Incident Details
        String natureOfComplaint,
        LocalDate dateOfIncident,
        LocalTime timeOfIncident,
        String placeOfIncident,
        String narrativeStatement,


        // Evidence
        List<String> evidenceNames

) {}