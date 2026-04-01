package com.barangay.barangay.blotter.service;
import java.time.LocalDate;

import com.barangay.barangay.admin_management.model.User;
import com.barangay.barangay.audit.service.AuditLogService;
import com.barangay.barangay.blotter.dto.complaint.FormalComplaintEntry;
import com.barangay.barangay.blotter.dto.complaint.RecordBlotterEntry;
import com.barangay.barangay.blotter.dto.complaint.WitnessDTO;
import com.barangay.barangay.blotter.model.*;
import com.barangay.barangay.blotter.model.EvidenceType;
import com.barangay.barangay.blotter.repository.*;
import com.barangay.barangay.department.model.Department;
import com.barangay.barangay.enumerated.*;
import com.barangay.barangay.person.model.Complainant;
import com.barangay.barangay.person.model.Person;
import com.barangay.barangay.person.model.Respondent;
import com.barangay.barangay.person.model.Witness;
import com.barangay.barangay.person.repository.ComplainantRepository;
import com.barangay.barangay.person.repository.PersonRepository;
import com.barangay.barangay.person.repository.RespondentRepository;
import com.barangay.barangay.person.repository.WitnessRepository;
import com.barangay.barangay.user_management.repository.UserManagementRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class BlotterFormComplaintService {

    private final BlotterCaseRepository blotterRepository;
    private final PersonRepository personRepository;
    private final ComplainantRepository complainantRepository;
    private final RespondentRepository respondentRepository;
    private final IncidentDetailRepository incidentDetailRepository;
    private final NarrativeRepository narrativeRepository;
    private final AuditLogService auditLogService;
    private final UserManagementRepository UserManagementRepository;
    private final EvidenceRecordRepository evidenceRecordRepository;
    private final EvidenceTypeRepository evidenceTypeRepository;
    private final ObjectMapper objectMapper;
    private final CasteTimeLineRepository  caseTimeLineRepository;
    private final WitnessRepository witnessRepository;

    @Transactional
    public String saveForTheRecord(RecordBlotterEntry dto, User officer, String ipAddress) {
        User managedOfficer = UserManagementRepository.findById(officer.getId())
                .orElseThrow(() -> new RuntimeException("Officer not found."));
        validateOfficerAccess(managedOfficer);

        Department userDept = managedOfficer.getAllowedDepartments().stream()
                .findFirst()
                .orElseThrow(() -> new RuntimeException("No assigned department."));

        Person complainantPerson = getOrSavePeople(
                dto.complainantId(),
                dto.firstName(), dto.lastName(), dto.middleName(),
                dto.contactNumber(), dto.completeAddress(), dto.gender(), null
        );

        Person respondentPerson = getOrSavePeople(
                dto.respondentId(),
                dto.respondentFirstName(), dto.respondentLastName(), dto.respondentMiddleName(),
                dto.respondentContact(), dto.respondentAddress(), null, null
        );

        // 3. INITIALIZE BLOTTER CASE (HEADER)
        BlotterCase blotter = new BlotterCase();
        blotter.setBlotterNumber(generateBlotterNumber());
        blotter.setCaseType(CaseType.FOR_THE_RECORD);
        blotter.setStatus(CaseStatus.RECORDED);
        blotter.setDateFiled(LocalDateTime.now());
        blotter.setDepartment(userDept);
        blotter.setCreatedBy(managedOfficer);
        blotter.setCertifiedAt(LocalDateTime.now());
        blotter.setIsCertified(true);

        // 4. COMPLAINANT (Unidirectional Link)
        Complainant complainant = new Complainant();
        complainant.setPerson(complainantPerson);
        // TANDAAN: Wala itong .setBlotterCase() sa entity mo!
        blotter.setComplainant(complainant);

        // 5. RESPONDENT (Unidirectional Link)
        Respondent respondent = new Respondent();
        respondent.setPerson(respondentPerson);
        respondent.setRelationshipToComplainant(dto.relationshipToComplainant());
        blotter.setRespondent(respondent);

        // 6. NARRATIVE
        Narrative narrative = new Narrative();
        narrative.setStatement(dto.narrativeStatement());
        // Ginamit ang Java field name: narrativeStatement (Kahit 'statemet' ang DB column)
        blotter.setNarrativeStatement(narrative);

        // 7. INCIDENT DETAIL
        IncidentDetail incident = new IncidentDetail();
        incident.setNatureOfComplaint(String.valueOf(dto.natureOfComplaintId()));
        incident.setPlaceOfIncident(dto.completeAddress());
        incident.setDateOfIncident(dto.dateOfIncident());
        incident.setTimeOfIncident(dto.timeOfIncident());
        blotter.setIncidentDetail(incident);

        // 8. FINAL SAVE (Parent handles everything through CascadeType.ALL)
        // Dito pa lang natin ise-save para may ID na ang blotter bago ang Evidence
        BlotterCase savedBlotter = blotterRepository.save(blotter);

        // 9. EVIDENCE (Special Case: ManyToOne needs manual saving after Parent has ID)
        if (dto.evidenceTypeIds() != null) {
            saveEvidenceRecords(dto.evidenceTypeIds(), savedBlotter, managedOfficer);
        }

        logDetailedActivity(managedOfficer, savedBlotter, complainantPerson, null, ipAddress);

        return savedBlotter.getBlotterNumber();
    }

    private void saveEvidenceRecords(List<Long> evidenceTypeIds, BlotterCase blotter, User officer) {
        if (evidenceTypeIds != null && !evidenceTypeIds.isEmpty()) {
            for (Long typeId : evidenceTypeIds) {
                EvidenceType eType = evidenceTypeRepository.findById(typeId)
                        .orElseThrow(() -> new RuntimeException("Evidence Type not found."));

                EvidenceRecord er = new EvidenceRecord();
                er.setBlotterCase(blotter); // Bidirectional link works here
                er.setType(eType);
                er.setReceivedBy(officer);
                evidenceRecordRepository.save(er);
            }
        }
    }

    @Transactional
    public String fileFormalComplaint(FormalComplaintEntry dto, User officer, String ipAddress) {

        // 1. RE-FETCH & SECURITY
        // Ginagamit ang findById dahil 'createdBy' ang nasa entity mo, hindi receivingOfficer
        User managedOfficer = UserManagementRepository.findById(officer.getId())
                .orElseThrow(() -> new RuntimeException("Officer not found."));
        validateOfficerAccess(managedOfficer);

        Department userDept = managedOfficer.getAllowedDepartments().stream()
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Officer has no assigned department."));

        // 2. PEOPLE MASTER - COMPLAINANT
        Person complainantPerson = getOrSavePeople(
                dto.complainantId(),
                dto.complainantFirstName(),
                dto.complainantLastName(),
                dto.complainantMiddleName(),
                dto.complainantContact(),
                dto.complainantAddress(),
                dto.complainantGender(),
                null
        );

        // 3. INITIALIZE BLOTTER CASE (PARENT)
        BlotterCase blotter = new BlotterCase();
        blotter.setBlotterNumber(generateBlotterNumber());
        blotter.setCaseType(CaseType.FORMAL_COMPLAINT);
        blotter.setStatus(CaseStatus.PENDING);
        blotter.setDateFiled(LocalDateTime.now());
        blotter.setDepartment(userDept);
        blotter.setCreatedBy(managedOfficer);
        blotter.setCertifiedAt(LocalDateTime.now());
        blotter.setIsCertified(true);

        // 4. COMPLAINANT (Link to Owner)
        Complainant cLink = new Complainant();
        cLink.setPerson(complainantPerson);
        // Tandaan: Unidirectional ito base sa model mo, kaya sa blotter lang i-set
        blotter.setComplainant(cLink);

        // 5. RESPONDENT
        Person respondentPerson = getOrSavePeople(
                dto.respondentId(),
                dto.respondentFirstName(),
                dto.respondentLastName(),
                dto.respondentMiddleName(),
                dto.respondentContact(),
                dto.respondentAddress(),
                dto.respondentGender(),
                dto.respondentDob()
        );

        Respondent rLink = new Respondent();
        rLink.setPerson(respondentPerson);
        rLink.setAlias(dto.respondentAlias());
        rLink.setLivingWithComplainant(dto.livingWithComplainant());
        rLink.setRelationshipToComplainant(dto.relationshipTypeName());

        blotter.setRespondent(rLink);
        Narrative narrative = new Narrative();
        narrative.setStatement(dto.narrativeStatement());
        blotter.setNarrativeStatement(narrative);

        IncidentDetail incident = new IncidentDetail();
        incident.setNatureOfComplaint(String.valueOf(dto.natureOfComplaintId()));
        incident.setDateOfIncident(dto.dateOfIncident());
        incident.setTimeOfIncident(dto.timeOfIncident());
        incident.setPlaceOfIncident(dto.placeOfIncident());
        incident.setInjuriesDamagesDescription(dto.descriptionOfInjuries());

        if (dto.frequencyOfIncident() != null) {
            incident.setFrequency(String.valueOf(dto.frequencyOfIncident()));
        }
        blotter.setIncidentDetail(incident);

        if (dto.witnesses() != null) {
            for (WitnessDTO wDto : dto.witnesses()) {
                Person witnessPerson = getOrSavePeople(
                        wDto.personId(), wDto.fullName(), "Witness", "",
                        wDto.contactNumber(), wDto.address(), null, null
                );

                Witness witness = new Witness();
                witness.setPerson(witnessPerson);
                witness.setTestimony(wDto.testimony());
                witness.setBlotterCase(blotter); // Link child to parent
                blotter.getWitnesses().add(witness); // Add to parent's list
            }
        }

        // 9. TIMELINE
        CaseTimeline timeline = new CaseTimeline();
        timeline.setBlotterCase(blotter);
        timeline.setEventType(TimelineEventType.CASE_FILED);
        timeline.setTitle("Case Officially Filed");
        timeline.setDescription("Case filed by officer: " + managedOfficer.getPerson().getLastName());
        timeline.setPerformedBy(managedOfficer);
        // I-save muna si blotter para sa foreign keys ng timeline at witnesses
        BlotterCase savedCase = blotterRepository.save(blotter);

        // Save timeline separately dahil ManyToOne ito
        caseTimeLineRepository.save(timeline);

        return savedCase.getBlotterNumber();
    }


    private Person getOrSavePeople(Long id, String first, String last, String middle, String contact, String address, String gender, LocalDate dob) {
        if (id != null && id != 0L) {
            return personRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Person with ID " + id + " not found."));
        }

        if (first == null || first.isBlank()) {
            throw new RuntimeException("First name is required for manual entry.");
        }

        Person newPerson = new Person();
        newPerson.setFirstName(first);
        newPerson.setLastName(last != null ? last : "Dayo/Unidentified");
        newPerson.setMiddleName(middle);
        newPerson.setContactNumber(contact);
        newPerson.setCompleteAddress(address);
        newPerson.setGender(gender);
        newPerson.setBirthDate(dob);
        newPerson.setIsResident(false); // Default pag manual entry sa blotter

        return personRepository.save(newPerson);
    }








    private void validateOfficerAccess(User officer) {
        if (officer.getRole() != null && officer.getRole().getRoleName().equalsIgnoreCase("ROOT_ADMIN")) {
            return;
        }

        boolean isBlotterDept = officer.getAllowedDepartments().stream()
                .anyMatch(d -> d.getName().equalsIgnoreCase("BLOTTER") || d.getId() == 3L);

        boolean hasCreatePerm = officer.getCustomPermissions().stream()
                .anyMatch(p -> p.getPermissionName().equalsIgnoreCase("Create Blotter Entry"));

        if (!isBlotterDept || !hasCreatePerm) {
            throw new RuntimeException("Unauthorized: Access denied. User must be in the Blotter Department with 'Create Records' permission.");
        }
    }




    private String generateBlotterNumber() {
        return LocalDateTime.now().getYear() + "-BLT-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    private void logDetailedActivity(User officer, BlotterCase bc, Person p, RecordBlotterEntry dto, String ip) {
        try {
            Map<String, Object> snapshot = new LinkedHashMap<>();
            snapshot.put("Case Number", bc.getBlotterNumber());
            snapshot.put("Complainant", p.getFirstName() + " " + p.getLastName());
            snapshot.put("Respondent", dto.respondentFirstName() + " " + dto.respondentLastName());
            snapshot.put("Incident Date", dto.dateOfIncident());

            String snippet = dto.narrativeStatement();
            if (snippet != null && snippet.length() > 80) {
                snippet = snippet.substring(0, 80) + "...";
            }
            snapshot.put("Narrative Snippet", snippet);

            // PRETTY PRINTING LOGIC
            String jsonState = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(snapshot);

            auditLogService.log(
                    officer,
                    Departments.BLOTTER,
                    "BLOTTER_ENTRY",
                    Severity.INFO,
                    "CREATE_BLOTTER_FOR_RECORD",
                    ip,
                    "Created blotter entry [" + bc.getBlotterNumber() + "]",
                    null,
                    jsonState
            );
        } catch (Exception e) {
            auditLogService.log(officer, null, "ERROR", Severity.CRITICAL, "LOG_FAIL", ip, e.getMessage(), null, null);
        }

    }






}