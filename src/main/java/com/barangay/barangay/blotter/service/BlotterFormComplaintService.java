package com.barangay.barangay.blotter.service;

import com.barangay.barangay.admin_management.model.User;
import com.barangay.barangay.audit.service.AuditLogService;
import com.barangay.barangay.blotter.dto.FormalComplaintEntry;
import com.barangay.barangay.blotter.dto.RecordBlotterEntry;
import com.barangay.barangay.blotter.model.*;
import com.barangay.barangay.blotter.model.EvidenceType;
import com.barangay.barangay.blotter.repository.*;
import com.barangay.barangay.department.model.Department;
import com.barangay.barangay.enumerated.*;
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

    @Transactional
    public String saveForTheRecord(RecordBlotterEntry dto, User officer, String ipAddress) {

        User managedOfficer = UserManagementRepository.findByIdWithDepartments(officer.getId())
                .orElseThrow(() -> new RuntimeException("Officer not found in database."));

        Department userDept = managedOfficer.getAllowedDepartments().stream()
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Officer has no assigned department."));

        // 1. SECURITY & PERMISSION CHECK
        validateOfficerAccess(managedOfficer);

        // 2. SAVE COMPLAINANT (People Table)
        People complainantPerson = new People();
        complainantPerson.setFirstName(dto.firstName());
        complainantPerson.setLastName(dto.lastName());
        complainantPerson.setMiddleName(dto.middleName());
        complainantPerson.setContactNumber(dto.contactNumber());
        complainantPerson.setCompleteAddress(dto.completeAddress());
        complainantPerson.setAge(dto.age() != null ? dto.age().shortValue() : null);
        complainantPerson.setGender(dto.gender());
        complainantPerson.setCivilStatus(dto.civilStatus());
        complainantPerson.setEmail(dto.email());

        peopleRepository.save(complainantPerson);

        People respondentPerson = new People();
        respondentPerson.setFirstName(dto.respondentFirstName());
        respondentPerson.setLastName(dto.respondentLastName());
        respondentPerson.setMiddleName(dto.respondentMiddleName());
        respondentPerson.setContactNumber(dto.respondentContact());
        respondentPerson.setCompleteAddress(dto.respondentAddress());
        peopleRepository.save(respondentPerson);






        // 3. CREATE BLOTTER CASE HEADER
        BlotterCase blotter = new BlotterCase();
        blotter.setBlotterNumber(generateBlotterNumber());
        blotter.setCaseType(CaseType.FOR_THE_RECORD);
        blotter.setStatus(CaseStatus.RECORDED);
        blotter.setDateFiled(LocalDateTime.now());
        blotter.setDepartment(userDept);

        blotter.setCreatedBy(officer);
        blotter.setReceivingOfficer(managedOfficer);
        blotter.setCreatedBy(managedOfficer);
        managedOfficer.getAllowedDepartments().stream()
                .filter(d -> d.getName().equalsIgnoreCase("BLOTTER"))
                .findFirst()
                .ifPresent(blotter::setDepartment);

        blotter.setReceivingOfficer(managedOfficer);
        blotterRepository.save(blotter);

        for (String inputName : dto.evidenceTypeIds()) {
            EvidenceType eType = evidenceTypeRepository.findByTypeName(inputName)
                    .orElseGet(() -> {
                        EvidenceType newType = new EvidenceType();
                        newType.setTypeName(inputName);
                        return evidenceTypeRepository.save(newType);
                    });

            EvidenceRecord er = new EvidenceRecord();
            er.setBlotterCase(blotter);
            er.setType(eType);
            er.setReceivedBy(managedOfficer);
            evidenceRecordRepository.save(er);
        }

        // 4. LINK PERSON TO COMPLAINANT TABLE
        Complainant complainant = new Complainant();
        complainant.setBlotterCase(blotter);
        complainant.setPerson(complainantPerson);

        complainantRepository.save(complainant);



        Respondent rLink = new Respondent();
        rLink.setBlotterCase(blotter);
        rLink.setPerson(respondentPerson);
        RelationshipType relType = relationshipTypeRepository.findByNameIgnoreCase(dto.relationshipToComplainant())
                .orElseGet(() -> {
                    RelationshipType newType = new RelationshipType();
                    newType.setName(dto.relationshipToComplainant().trim());
                    return relationshipTypeRepository.save(newType);
                });
        rLink.setRelationshipType(relType);
        respondentRepository.save(rLink);




        //  SAVE INCIDENT DETAILS & NARRATIVE
        saveIncidentAndNarrative(dto, blotter);

        //  STRUCTURED AUDIT LOG (JSON Snapshot)
        logDetailedActivity(officer, blotter, complainantPerson, dto, ipAddress);

        return blotter.getBlotterNumber();
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

        // 2. SAVE COMPLAINANT
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
        peopleRepository.save(complainant);

        BlotterCase blotter = new BlotterCase();
        blotter.setBlotterNumber(generateBlotterNumber());
        blotter.setCaseType(CaseType.FORMAL_COMPLAINT);
        blotter.setStatus(CaseStatus.PENDING);
        blotter.setDateFiled(LocalDateTime.now());
        blotter.setReceivingOfficer(managedOfficer);
        blotter.setDepartment(userDept);
        blotter.setCreatedBy(managedOfficer);


        blotterRepository.save(blotter);

        CaseTimeline timeline = new CaseTimeline();
        timeline.setBlotterCase(blotter);
        timeline.setEventType(TimelineEventType.CASE_FILED);
        timeline.setTitle("Case Officially Filed");
        timeline.setDescription("Case was received and filed by " + blotter.getReceivingOfficer().getLastName());
        timeline.setPerformedBy(blotter.getCreatedBy());
        caseTimeLineRepository.save(timeline);



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
            er.setBlotterCase(blotter);
            er.setType(eType);
            er.setReceivedBy(managedOfficer);
            evidenceRecordRepository.save(er);
        }

        Complainant cLink = new Complainant();
        cLink.setBlotterCase(blotter);
        cLink.setPerson(complainant);
        complainantRepository.save(cLink);

        People respondentPerson = new People();
        respondentPerson.setLastName(dto.respondentLastName());
        respondentPerson.setFirstName(dto.respondentFirstName());
        respondentPerson.setMiddleName(dto.respondentMiddleName());
        respondentPerson.setCompleteAddress(dto.respondentAddress());
        respondentPerson.setContactNumber(dto.respondentContact());
        respondentPerson.setGender(dto.respondentGender());
        respondentPerson.setAge(dto.respondentAge());
        respondentPerson.setCivilStatus(dto.respondentCivilStatus());
        peopleRepository.save(respondentPerson);

        Respondent rLink = new Respondent();
        rLink.setBlotterCase(blotter);
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

        // 6. INCIDENT DETAILS
        IncidentDetail incident = new IncidentDetail();
        incident.setBlotterCase(blotter);
        incident.setNatureOfComplaint(natureRepository.findById(dto.natureOfComplaintId())
                .orElseThrow(() -> new RuntimeException("Nature not found")));
        incident.setDateOfIncident(dto.dateOfIncident());
        incident.setTimeOfIncident(dto.timeOfIncident());
        incident.setPlaceOfIncident(dto.placeOfIncident());
        incident.setInjuriesDamagesDescription(dto.descriptionOfInjuries());
        incidentDetailRepository.save(incident);

        IncidentFrequency frequency = new IncidentFrequency();
        frequency.setLabel(dto.frequencyOfIncident());
        incidentFrequencyRepository.save(frequency);


        // 7. NARRATIVE
        Narrative narrative = new Narrative();
        narrative.setBlotterCase(blotter);
        narrative.setStatement(dto.narrativeStatement());
        narrativeRepository.save(narrative);

        // 8. LOGGING
        logDetailedActivity(managedOfficer, blotter, complainant, null, ipAddress);

        return blotter.getBlotterNumber();
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