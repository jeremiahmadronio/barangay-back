package com.barangay.barangay.lupon.dto;

import com.barangay.barangay.blotter.dto.complaint.WitnessDTO;
import com.barangay.barangay.enumerated.CaseStatus;
import com.barangay.barangay.enumerated.CaseType;
import java.time.LocalDateTime;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public record LuponViewDTO(
        Long id,
        String blotterNumber,
        CaseType caseType,
        CaseStatus caseStatus,
        String caseStatusRemarks,

        LocalDateTime dateFiled,
        LocalDateTime referredToLuponAt,
        String blotterReceivingOfficer,
        String caseAssignTo,

        MediationInfoDTO mediationInfo,
        PersonDTO complainant,
        RespondentDTO respondent,
        IncidentDetailDTO incidentDetail,

        String narrative,
        List<String> evidenceTypeIds,
        List<WitnessDTO> witnesses,
        List<LuponCaseMemberHandlerDTO> memberHandlers
) {

    public record MediationInfoDTO(
            LocalDateTime luponDeadline,
            long daysRemaining,
            Integer extensionCount,
            LocalDateTime extensionDate,
            String extensionReason,
            String settlementTerms
    ) {}

    public record PersonDTO(
            String firstName,
            String lastName,
            String middleName,
            String contactNumber,
            Integer age,
            String gender,
            String civilStatus,
            String email,
            String completeAddress
    ) {}

    public record RespondentDTO(
            String firstName,
            String lastName,
            String middleName,
            String alias,
            String contactNumber,
            Integer age,
            String gender,
            LocalDate dateOfBirth,
            String civilStatus,
            String occupation,
            String relationshipToComplainant,
            String address,
            boolean livingWithComplainant
    ) {}

    public record IncidentDetailDTO(
            String natureOfComplaint,
            LocalDate incidentDate,
            LocalTime incidentTime,
            String incidentLocation,
            String frequencyOfIncident,
            String descriptionOfInjuries
    ) {}
}