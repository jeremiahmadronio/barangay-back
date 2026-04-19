package com.barangay.barangay.blotter.dto.complaint;

import com.barangay.barangay.blotter.model.BlotterCase;
import com.barangay.barangay.blotter.model.EvidenceRecord;
import com.barangay.barangay.person.model.Person;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public record CaseSnapshot(
        String blotterNumber,
        String complainantName,
        String complainantContact,
        String complainantAddress,
        String respondentName,
        String respondentContact,
        String respondentAddress,
        String alias,
        String relationshipToComplainant,
        Boolean livingWithComplainant,
        String natureOfComplaint,
        LocalDate dateOfIncident,
        LocalTime timeOfIncident,
        String placeOfIncident,
        String frequency,
        String injuriesDescription,
        String assignedEmployee,
        List<String> witnesses,
        List<String> evidenceTypes
) {
    public static CaseSnapshot from(BlotterCase c, List<EvidenceRecord> evidenceRecords) {
        Person complainantPerson = c.getComplainant() != null ? c.getComplainant().getPerson() : null;
        Person respondentPerson = c.getRespondent() != null ? c.getRespondent().getPerson() : null;

        return new CaseSnapshot(
                c.getBlotterNumber(),

                complainantPerson != null
                        ? complainantPerson.getLastName() + ", " + complainantPerson.getFirstName()
                        : null,
                complainantPerson != null ? complainantPerson.getContactNumber() : null,
                complainantPerson != null ? complainantPerson.getCompleteAddress() : null,

                respondentPerson != null
                        ? respondentPerson.getLastName() + ", " + respondentPerson.getFirstName()
                        : null,
                respondentPerson != null ? respondentPerson.getContactNumber() : null,
                respondentPerson != null ? respondentPerson.getCompleteAddress() : null,

                c.getRespondent() != null ? c.getRespondent().getAlias() : null,
                c.getRespondent() != null ? c.getRespondent().getRelationshipToComplainant() : null,
                c.getRespondent() != null ? c.getRespondent().getLivingWithComplainant() : null,

                c.getIncidentDetail() != null ? c.getIncidentDetail().getNatureOfComplaint() : null,
                c.getIncidentDetail() != null ? c.getIncidentDetail().getDateOfIncident() : null,
                c.getIncidentDetail() != null ? c.getIncidentDetail().getTimeOfIncident() : null,
                c.getIncidentDetail() != null ? c.getIncidentDetail().getPlaceOfIncident() : null,
                c.getIncidentDetail() != null ? c.getIncidentDetail().getFrequency() : null,
                c.getIncidentDetail() != null ? c.getIncidentDetail().getInjuriesDamagesDescription() : null,

                c.getEmployee() != null
                        ? c.getEmployee().getPerson().getLastName() + ", " + c.getEmployee().getPerson().getFirstName()
                        : null,

                c.getWitnesses() != null
                        ? c.getWitnesses().stream()
                        .filter(w -> w.getPerson() != null)
                        .map(w -> w.getPerson().getLastName() + ", " + w.getPerson().getFirstName())
                        .toList()
                        : List.of(),

                evidenceRecords != null
                        ? evidenceRecords.stream()
                        .filter(e -> e.getType() != null)
                        .map(e -> e.getType().getTypeName())
                        .toList()
                        : List.of()
        );
    }

}