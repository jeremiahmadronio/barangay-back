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
import com.barangay.barangay.resident.model.Complainant;
import com.barangay.barangay.resident.model.People;
import com.barangay.barangay.resident.model.Respondent;
import com.barangay.barangay.resident.model.Witness;
import com.barangay.barangay.resident.repository.ComplainantRepository;
import com.barangay.barangay.resident.repository.PeopleRepository;
import com.barangay.barangay.resident.repository.RespondentRepository;
import com.barangay.barangay.resident.repository.WitnessRepository;
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
    private final PeopleRepository peopleRepository;
    private final ComplainantRepository complainantRepository;
    private final RespondentRepository respondentRepository;
    private final IncidentDetailRepository incidentDetailRepository;
    private final NarrativeRepository narrativeRepository;
    private final NatureOfComplaintRepository natureRepository;
    private final RelationshipTypeRepository relationshipTypeRepository;
    private final AuditLogService auditLogService;
    private final UserManagementRepository UserManagementRepository;
    private final EvidenceRecordRepository evidenceRecordRepository;
    private final EvidenceTypeRepository evidenceTypeRepository;
    private final ObjectMapper objectMapper;
    private final CasteTimeLineRepository  caseTimeLineRepository;
    private final IncidentFrequencyRepository incidentFrequencyRepository;
    private final WitnessRepository witnessRepository;

    @Transactional
    public String saveForTheRecord(RecordBlotterEntry dto, User officer, String ipAddress) {

        User managedOfficer = UserManagementRepository.findByIdWithDepartments(officer.getId())
                .orElseThrow(() -> new RuntimeException("Officer not found."));
        validateOfficerAccess(managedOfficer);

        Department userDept = managedOfficer.getAllowedDepartments().stream()
                .findFirst()
                .orElseThrow(() -> new RuntimeException("No assigned department."));


        People complainantPerson = getOrSavePeople(
                dto.complainantId(),
                dto.firstName(), dto.lastName(), dto.middleName(),
                dto.contactNumber(), dto.completeAddress(), dto.gender(), null
        );


        People respondentPerson = getOrSavePeople(
                dto.respondentId(),
                dto.respondentFirstName(), dto.respondentLastName(), dto.respondentMiddleName(),
                dto.respondentContact(), dto.respondentAddress(), null, null
        );

        // 4. CREATE BLOTTER CASE HEADER
        BlotterCase blotter = new BlotterCase();
        blotter.setBlotterNumber(generateBlotterNumber());
        blotter.setCaseType(CaseType.FOR_THE_RECORD);
        blotter.setStatus(CaseStatus.RECORDED);
        blotter.setDateFiled(LocalDateTime.now());
        blotter.setDepartment(userDept);
        blotter.setReceivingOfficer(managedOfficer);
        blotter.setCreatedBy(managedOfficer);
        blotter.setCertifiedAt(LocalDateTime.now());
        blotterRepository.save(blotter);

        // 5. LINK ROLES (Complainant & Respondent)
        Complainant complainant = new Complainant();
        complainant.setBlotterCase(blotter);
        complainant.setPerson(complainantPerson);
        complainantRepository.save(complainant);

        Respondent rLink = new Respondent();
        rLink.setBlotterCase(blotter);
        rLink.setPerson(respondentPerson);

        if (dto.relationshipToComplainant() != null) {
            RelationshipType relType = relationshipTypeRepository.findByNameIgnoreCase(dto.relationshipToComplainant().trim())
                    .orElseGet(() -> {
                        RelationshipType newType = new RelationshipType();
                        newType.setName(dto.relationshipToComplainant().trim());
                        return relationshipTypeRepository.save(newType);
                    });
            rLink.setRelationshipType(relType);
        }
        respondentRepository.save(rLink);

        saveEvidenceRecords(dto.evidenceTypeIds(), blotter, managedOfficer);
        saveIncidentAndNarrative(dto, blotter);

        logDetailedActivity(managedOfficer, blotter, complainantPerson, null, ipAddress);

        return blotter.getBlotterNumber();
    }

    private void saveEvidenceRecords(List<String> evidenceTypeIds, BlotterCase blotter, User officer) {
        if (evidenceTypeIds != null) {
            for (String input : evidenceTypeIds) {
                EvidenceType eType = evidenceTypeRepository.findByTypeName(input)
                        .orElseGet(() -> {
                            EvidenceType newType = new EvidenceType();
                            newType.setTypeName(input);
                            return evidenceTypeRepository.save(newType);
                        });

                EvidenceRecord er = new EvidenceRecord();
                er.setBlotterCase(blotter);
                er.setType(eType);
                er.setReceivedBy(officer);
                evidenceRecordRepository.save(er);
            }
        }
    }


    @Transactional
    public String fileFormalComplaint(FormalComplaintEntry dto, User officer, String ipAddress) {

        // 1. RE-FETCH & SECURITY
        User managedOfficer = UserManagementRepository.findByIdWithDepartments(officer.getId())
                .orElseThrow(() -> new RuntimeException("Officer not found."));
        validateOfficerAccess(managedOfficer);

        Department userDept = managedOfficer.getAllowedDepartments().stream()
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Officer has no assigned department."));


        People complainantPerson = getOrSavePeople(
                dto.complainantId(),
                dto.complainantFirstName(),
                dto.complainantLastName(),
                dto.complainantMiddleName(),
                dto.complainantContact(),
                dto.complainantAddress(),
                dto.complainantGender(),
                null // Complainant DOB is often not required in walk-ins
        );

        // 3. INITIALIZE BLOTTER CASE
        BlotterCase blotter = new BlotterCase();
        blotter.setBlotterNumber(generateBlotterNumber());
        blotter.setCaseType(CaseType.FORMAL_COMPLAINT);
        blotter.setStatus(CaseStatus.PENDING);
        blotter.setDateFiled(LocalDateTime.now());
        blotter.setReceivingOfficer(managedOfficer);
        blotter.setDepartment(userDept);
        blotter.setCertifiedAt(LocalDateTime.now());
        blotter.setCreatedBy(managedOfficer);
        blotterRepository.save(blotter);

        CaseTimeline timeline = new CaseTimeline();
        timeline.setBlotterCase(blotter);
        timeline.setEventType(TimelineEventType.CASE_FILED);
        timeline.setTitle("Case Officially Filed");
        timeline.setDescription("Case was received and filed by " + blotter.getReceivingOfficer().getLastName());
        timeline.setPerformedBy(blotter.getCreatedBy());
        caseTimeLineRepository.save(timeline);

        // 5. LINK COMPLAINANT TO CASE
        Complainant cLink = new Complainant();
        cLink.setBlotterCase(blotter);
        cLink.setPerson(complainantPerson);
        complainantRepository.save(cLink);

        // 6. GET OR CREATE RESPONDENT (PEOPLE MASTER)
        People respondentPerson = getOrSavePeople(
                dto.respondentId(),
                dto.respondentFirstName(),
                dto.respondentLastName(),
                dto.respondentMiddleName(),
                dto.respondentContact(),
                dto.respondentAddress(),
                dto.respondentGender(),
                dto.respondentDob() // Respondent DOB is important for identification
        );

        // 7. SAVE RESPONDENT DETAILS
        Respondent rLink = new Respondent();
        rLink.setBlotterCase(blotter);
        rLink.setPerson(respondentPerson);

        RelationshipType relType = relationshipTypeRepository.findByNameIgnoreCase(dto.relationshipTypeName())
                .orElseGet(() -> {
                    RelationshipType newType = new RelationshipType();
                    newType.setName(dto.relationshipTypeName().trim());
                    return relationshipTypeRepository.save(newType);
                });

        rLink.setRelationshipType(relType);
        rLink.setAlias(dto.respondentAlias());
        rLink.setOccupation(dto.respondentOccupation());
        rLink.setLivingWithComplainant(dto.livingWithComplainant());
        respondentRepository.save(rLink);

        // 8. INCIDENT DETAILS
        IncidentDetail incident = new IncidentDetail();
        incident.setBlotterCase(blotter);
        incident.setNatureOfComplaint(natureRepository.findById(dto.natureOfComplaintId())
                .orElseThrow(() -> new RuntimeException("Nature of Complaint not found")));
        incident.setDateOfIncident(dto.dateOfIncident());
        incident.setTimeOfIncident(dto.timeOfIncident());
        incident.setPlaceOfIncident(dto.placeOfIncident());
        incident.setInjuriesDamagesDescription(dto.descriptionOfInjuries());
        incidentDetailRepository.save(incident);

        // 9. WITNESS HANDLING (THE SMART WAY)
        if (dto.witnesses() != null && !dto.witnesses().isEmpty()) {
            for (WitnessDTO wDto : dto.witnesses()) {
                // Isang search result lang ang fullName sa WitnessDTO,
                // kaya gagamitin natin itong First Name para sa manual entry
                People witnessPerson = getOrSavePeople(
                        wDto.personId(),
                        wDto.fullName(), // Manual name
                        "Witness",       // Placeholder Last Name if manual
                        "",
                        wDto.contactNumber(),
                        wDto.address(),
                        null, null
                );

                Witness witness = new Witness();
                witness.setBlotterCase(blotter);
                witness.setPerson(witnessPerson); // LINKED TO PEOPLE MASTER
                witness.setTestimony(wDto.testimony());
                witnessRepository.save(witness);
            }
        }

        IncidentFrequency frequency = new IncidentFrequency();
        frequency.setLabel(dto.frequencyOfIncident());
        incidentFrequencyRepository.save(frequency);

        return blotter.getBlotterNumber();
    }


    private People getOrSavePeople(Long id, String first, String last, String middle, String contact, String address, String gender, LocalDate dob) {
        if (id != null) {
            return peopleRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Person with ID " + id + " not found."));
        }

        // Brutal Check: Kung walang ID, dapat may pangalan man lang
        if (first == null || first.isBlank()) {
            throw new RuntimeException("First name is required for manual entry.");
        }

        People newPerson = new People();
        newPerson.setFirstName(first);
        newPerson.setLastName(last != null ? last : "Dayo/Unidentified");
        newPerson.setMiddleName(middle);
        newPerson.setContactNumber(contact);
        newPerson.setCompleteAddress(address);
        newPerson.setGender(gender);
        newPerson.setBirthDate(dob);
        newPerson.setIsResident(false); // Default pag manual entry sa blotter

        return peopleRepository.save(newPerson);
    }

    @Transactional
    public String escalateToFormalComplaint(String sourceBlotterNumber, FormalComplaintEntry dto, User officer, String ipAddress) {

        // 1. RE-FETCH & SECURITY
        User managedOfficer = UserManagementRepository.findByIdWithDepartments(officer.getId())
                .orElseThrow(() -> new RuntimeException("Officer not found."));
        validateOfficerAccess(managedOfficer);

        Department userDept = managedOfficer.getAllowedDepartments().stream()
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Officer has no assigned department."));

        // 2. FETCH & UPDATE EXISTING BLOTTER (ESCALATION PROCESS)
        BlotterCase existingBlotter = blotterRepository.findByBlotterNumber(sourceBlotterNumber)
                .orElseThrow(() -> new RuntimeException("Source Blotter not found: " + sourceBlotterNumber));

        // Palitan ang status ng lumang blotter
        // (Siguraduhing tama ang Enum name mo dito, pwedeng ESCALATED o ELEVATED_TO_FORMAL)
        existingBlotter.setStatus(CaseStatus.ELEVATED_TO_FORMAL);
        blotterRepository.save(existingBlotter);

        // I-log sa Timeline ng lumang blotter na na-escalate siya
        CaseTimeline oldTimeline = new CaseTimeline();
        oldTimeline.setBlotterCase(existingBlotter);
        oldTimeline.setEventType(TimelineEventType.ESCALATED); // Gamitin ang tamang Enum mo
        oldTimeline.setTitle("Escalated to Formal Complaint");
        oldTimeline.setDescription("Case was escalated to a formal complaint by " + managedOfficer.getLastName());
        oldTimeline.setPerformedBy(managedOfficer);
        caseTimeLineRepository.save(oldTimeline);

        // 3. CREATE NEW FORMAL COMPLAINT RECORD
        People complainant = getPeople(dto);
        peopleRepository.save(complainant);

        BlotterCase newBlotter = new BlotterCase();
        newBlotter.setBlotterNumber(generateBlotterNumber());
        newBlotter.setCaseType(CaseType.FORMAL_COMPLAINT);
        newBlotter.setStatus(CaseStatus.PENDING);
        newBlotter.setDateFiled(LocalDateTime.now());
        newBlotter.setReceivingOfficer(managedOfficer);
        newBlotter.setDepartment(userDept);
        newBlotter.setCertifiedAt(LocalDateTime.now());
        newBlotter.setCreatedBy(managedOfficer);
        blotterRepository.save(newBlotter);

        // 4. TIMELINE PARA SA BAGONG BLOTTER
        CaseTimeline newTimeline = new CaseTimeline();
        newTimeline.setBlotterCase(newBlotter);
        newTimeline.setEventType(TimelineEventType.CASE_FILED);
        newTimeline.setTitle("Formal Complaint Created");
        newTimeline.setDescription("Created from escalation of blotter: " + sourceBlotterNumber);
        newTimeline.setPerformedBy(managedOfficer);
        caseTimeLineRepository.save(newTimeline);

        // 5. EVIDENCE RECORDS
        for (String input : dto.evidenceTypeIds()) {
            EvidenceType eType;
            if (input.matches("-?\\d+(\\.\\d+)?")) {
                Long id = Long.parseLong(input);
                eType = evidenceTypeRepository.findById(id)
                        .orElseGet(() -> {
                            EvidenceType newType = new EvidenceType();
                            newType.setTypeName(input);
                            return evidenceTypeRepository.save(newType);
                        });
            } else {
                eType = evidenceTypeRepository.findByTypeName(input)
                        .orElseGet(() -> {
                            EvidenceType newType = new EvidenceType();
                            newType.setTypeName(input);
                            return evidenceTypeRepository.save(newType);
                        });
            }
            EvidenceRecord er = new EvidenceRecord();
            er.setBlotterCase(newBlotter);
            er.setType(eType);
            er.setReceivedBy(managedOfficer);
            evidenceRecordRepository.save(er);
        }

        // 6. SAVE COMPLAINANT LINK
        Complainant cLink = new Complainant();
        cLink.setBlotterCase(newBlotter);
        cLink.setPerson(complainant);
        complainantRepository.save(cLink);

        // 7. SAVE RESPONDENT LINK
        People respondentPerson = getRespondentPerson(dto);
        peopleRepository.save(respondentPerson);

        Respondent rLink = new Respondent();
        rLink.setBlotterCase(newBlotter);
        rLink.setPerson(respondentPerson);
        RelationshipType relType = relationshipTypeRepository.findByNameIgnoreCase(dto.relationshipTypeName())
                .orElseGet(() -> {
                    RelationshipType newType = new RelationshipType();
                    newType.setName(dto.relationshipTypeName().trim());
                    return relationshipTypeRepository.save(newType);
                });
        rLink.setDateOfBirth(dto.respondentDob());
        rLink.setAlias(dto.respondentAlias());
        rLink.setOccupation(dto.respondentOccupation());
        rLink.setRelationshipType(relType);
        respondentRepository.save(rLink);

        IncidentDetail incident = new IncidentDetail();
        incident.setBlotterCase(newBlotter);
        incident.setNatureOfComplaint(natureRepository.findById(dto.natureOfComplaintId())
                .orElseThrow(() -> new RuntimeException("Nature not found")));
        incident.setDateOfIncident(dto.dateOfIncident());
        incident.setTimeOfIncident(dto.timeOfIncident());
        incident.setPlaceOfIncident(dto.placeOfIncident());
        incident.setInjuriesDamagesDescription(dto.descriptionOfInjuries());


        IncidentFrequency frequency = incidentFrequencyRepository.findByLabelIgnoreCase(dto.frequencyOfIncident())
                .orElseGet(() -> {
                    IncidentFrequency newFreq = new IncidentFrequency();
                    newFreq.setLabel(dto.frequencyOfIncident());
                    return incidentFrequencyRepository.save(newFreq);
                });

           incident.setFrequency(frequency);

        incidentDetailRepository.save(incident);

        // 9. NARRATIVE
        Narrative narrative = new Narrative();
        narrative.setBlotterCase(newBlotter);
        narrative.setStatement(dto.narrativeStatement());
        narrativeRepository.save(narrative);


        logDetailedActivity(managedOfficer, newBlotter, complainant, null, ipAddress);

        return newBlotter.getBlotterNumber();
    }

    private static People getRespondentPerson(FormalComplaintEntry dto) {
        People respondentPerson = new People();
        respondentPerson.setLastName(dto.respondentLastName());
        respondentPerson.setFirstName(dto.respondentFirstName());
        respondentPerson.setMiddleName(dto.respondentMiddleName());
        respondentPerson.setCompleteAddress(dto.respondentAddress());
        respondentPerson.setContactNumber(dto.respondentContact());
        respondentPerson.setGender(dto.respondentGender());
        respondentPerson.setAge(dto.respondentAge());
        respondentPerson.setCivilStatus(dto.respondentCivilStatus());
        return respondentPerson;
    }

    private static People getPeople(FormalComplaintEntry dto) {
        People complainant = new People();
        complainant.setLastName(dto.complainantLastName());
        complainant.setFirstName(dto.complainantFirstName());
        complainant.setMiddleName(dto.complainantMiddleName());
        complainant.setContactNumber(dto.complainantContact());
        complainant.setAge(dto.complainantAge() != null ? dto.complainantAge().shortValue() : null);
        complainant.setGender(dto.complainantGender());
        complainant.setCompleteAddress(dto.complainantAddress());
        complainant.setCivilStatus(dto.complainantCivilStatus());
        complainant.setEmail(dto.complainantEmail());
        return complainant;
    }


    private void validateOfficerAccess(User officer) {
        if (officer.getRole() != null && officer.getRole().getRoleName().equalsIgnoreCase("ROOT_ADMIN")) {
            return;
        }

        boolean isBlotterDept = officer.getAllowedDepartments().stream()
                .anyMatch(d -> d.getName().equalsIgnoreCase("BLOTTER") || d.getId() == 3L);

        // Standard Permission Check
        boolean hasCreatePerm = officer.getCustomPermissions().stream()
                .anyMatch(p -> p.getPermissionName().equalsIgnoreCase("Create Records"));

        if (!isBlotterDept || !hasCreatePerm) {
            throw new RuntimeException("Unauthorized: Access denied. User must be in the Blotter Department with 'Create Records' permission.");
        }
    }


    private void saveIncidentAndNarrative(RecordBlotterEntry dto, BlotterCase blotter) {
        IncidentDetail details = new IncidentDetail();
        details.setBlotterCase(blotter);
        details.setDateOfIncident(dto.dateOfIncident());
        details.setTimeOfIncident(dto.timeOfIncident());
        details.setPlaceOfIncident(dto.placeOfIncident());

        NatureOfComplaint nature = natureRepository.findById(dto.natureOfComplaintId())
                .orElseThrow(() -> new RuntimeException("Invalid Nature of Complaint ID"));
        details.setNatureOfComplaint(nature);
        incidentDetailRepository.save(details);

        Narrative narrative = new Narrative();
        narrative.setBlotterCase(blotter);
        narrative.setStatement(dto.narrativeStatement());
        narrativeRepository.save(narrative);
    }

    private String generateBlotterNumber() {
        return LocalDateTime.now().getYear() + "-BLT-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    private void logDetailedActivity(User officer, BlotterCase bc, People p, RecordBlotterEntry dto, String ip) {
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