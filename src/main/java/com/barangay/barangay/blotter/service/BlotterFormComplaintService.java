package com.barangay.barangay.blotter.service;
import java.time.LocalDate;

import com.barangay.barangay.admin_management.model.User;
import com.barangay.barangay.audit.service.AuditLogService;
import com.barangay.barangay.blotter.dto.complaint.*;
import com.barangay.barangay.blotter.model.*;
import com.barangay.barangay.blotter.model.EvidenceType;
import com.barangay.barangay.blotter.repository.*;
import com.barangay.barangay.department.model.Department;
import com.barangay.barangay.employee.model.Employee;
import com.barangay.barangay.employee.repository.EmployeeRepository;
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
import com.barangay.barangay.vawc.dto.AssignOfficerOptionDTO;
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
    private final EmployeeRepository employeeRepository;


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
        blotter.setComplainant(complainant);

        Respondent respondent = new Respondent();
        respondent.setPerson(respondentPerson);
        respondent.setRelationshipToComplainant(dto.relationshipToComplainant());
        blotter.setRespondent(respondent);

        Narrative narrative = new Narrative();
        narrative.setStatement(dto.narrativeStatement());
        blotter.setNarrativeStatement(narrative);

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

        User managedOfficer = UserManagementRepository.findById(officer.getId())
                .orElseThrow(() -> new RuntimeException("Officer not found."));
        validateOfficerAccess(managedOfficer);

        Department userDept = managedOfficer.getAllowedDepartments().stream()
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Officer has no assigned department."));

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

        BlotterCase blotter = new BlotterCase();
        blotter.setBlotterNumber(generateBlotterNumber());
        blotter.setCaseType(CaseType.FORMAL_COMPLAINT);
        blotter.setStatus(CaseStatus.PENDING);
        blotter.setDepartment(userDept);
        blotter.setCreatedBy(managedOfficer);
        blotter.setCertifiedAt(LocalDateTime.now());
        blotter.setIsCertified(true);

        Complainant cLink = new Complainant();
        cLink.setPerson(complainantPerson);
        blotter.setComplainant(cLink);

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
        incident.setNatureOfComplaint(dto.natureOfComplaintId());
        incident.setDateOfIncident(dto.dateOfIncident());
        incident.setTimeOfIncident(dto.timeOfIncident());
        incident.setPlaceOfIncident(dto.placeOfIncident());
        incident.setInjuriesDamagesDescription(dto.descriptionOfInjuries());
        if (dto.frequencyOfIncident() != null) {
            incident.setFrequency(dto.frequencyOfIncident());
        }
        blotter.setIncidentDetail(incident);

        if (dto.assignToId() != null) {
            Employee assignedEmployee = employeeRepository.findById(dto.assignToId())
                    .orElseThrow(() -> new RuntimeException("Employee not found."));
            if (assignedEmployee.getStatus().equals(Status.INACTIVE)) {
                throw new RuntimeException("Assigned officer is inactive.");
            }
            blotter.setEmployee(assignedEmployee);
        }

        if (dto.witnesses() != null) {
            for (WitnessDTO wDto : dto.witnesses()) {
                Person witnessPerson = getOrSavePeople(
                        wDto.personId(), wDto.fullName(), "Witness", "",
                        wDto.contactNumber(), wDto.address(), null, null
                );
                Witness witness = new Witness();
                witness.setPerson(witnessPerson);
                witness.setTestimony(wDto.testimony());
                witness.setBlotterCase(blotter);
                blotter.getWitnesses().add(witness);
            }
        }

        BlotterCase savedCase = blotterRepository.save(blotter);

        if (dto.evidenceTypeIds() != null && !dto.evidenceTypeIds().isEmpty()) {
            List<EvidenceRecord> evidenceRecords = dto.evidenceTypeIds().stream().map(id -> {
                EvidenceType type = evidenceTypeRepository.findById(Long.parseLong(id))
                        .orElseThrow(() -> new RuntimeException("Evidence type not found: " + id));
                EvidenceRecord record = new EvidenceRecord();
                record.setBlotterCase(savedCase);
                record.setType(type);
                record.setReceivedBy(managedOfficer);
                return record;
            }).toList();
            evidenceRecordRepository.saveAll(evidenceRecords);
        }

        CaseTimeline timeline = new CaseTimeline();
        timeline.setBlotterCase(savedCase);
        timeline.setEventType(TimelineEventType.CASE_FILED);
        timeline.setTitle("Case Officially Filed");
        timeline.setDescription("Case filed by officer: " + managedOfficer.getPerson().getLastName());
        timeline.setPerformedBy(managedOfficer);
        caseTimeLineRepository.save(timeline);

        auditLogService.log(
                managedOfficer,
                Departments.BLOTTER,
                "Blotter Cases",
                Severity.INFO,
                "Formal Complaint Filed — " + savedCase.getBlotterNumber() + ".",
                ipAddress,
                null,
                null,
                null
        );

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
        newPerson.setIsResident(false);

        return personRepository.save(newPerson);
    }








    private void validateOfficerAccess(User officer) {
        if (officer.getRole() != null && officer.getRole().getRoleName().equalsIgnoreCase("ROOT_ADMIN")) {
            return;
        }

        boolean isBlotterDept = officer.getAllowedDepartments().stream()
                .anyMatch(d -> d.getName().equalsIgnoreCase("BLOTTER") || d.getId() == 3L);

        boolean hasCreatePerm = officer.getCustomPermissions().stream()
                .anyMatch(p -> p.getPermissionName().equalsIgnoreCase("Create Case Entry"));

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



    @Transactional(readOnly = true)
    public List<AssignOfficerOptionDTO> getBlotterComplaintOfficer() {
        return employeeRepository.findAssignOfficeBlotterrOptionDTO();
    }





    @Transactional
    public void updateCase(Long caseId, UpdateCaseDTO dto, String currentUsername, User officer, String ip) {
        BlotterCase blotterCase = blotterRepository.findByIdAndIsArchivedFalse(caseId)
                .orElseThrow(() -> new RuntimeException("Blotter case with ID " + caseId + " not found."));

        User updatedBy = UserManagementRepository.findByUsername(currentUsername)
                .orElseThrow(() -> new RuntimeException("User not found: " + currentUsername));

        List<EvidenceRecord> oldEvidenceRecords = evidenceRecordRepository.findAllByBlotterCase(blotterCase);
        String oldValue = toJson(CaseSnapshot.from(blotterCase, oldEvidenceRecords));

        applyComplainantUpdate(blotterCase.getComplainant(), dto);
        applyRespondentUpdate(blotterCase.getRespondent(), dto);
        applyIncidentDetailUpdate(blotterCase.getIncidentDetail(), dto);
        applyWitnessUpdate(blotterCase, dto.witnesses());
        applyEvidenceUpdate(blotterCase, dto.evidenceTypeIds(), updatedBy);
        applyAssigneeUpdate(blotterCase, dto.assignToId());

        blotterCase.setUpdatedBy(updatedBy);

        List<EvidenceRecord> newEvidenceRecords = evidenceRecordRepository.findAllByBlotterCase(blotterCase);
        String newValue = toJson(CaseSnapshot.from(blotterCase, newEvidenceRecords));

        auditLogService.log(
                officer,
                Departments.BLOTTER,
                "Blotter Cases",
                Severity.INFO,
                "Blotter Case Updated — " + blotterCase.getBlotterNumber() + ".",
                ip,
                null,
                oldValue,
                newValue
        );
    }

    private String toJson(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (Exception e) {
            return "{}";
        }
    }

    private void applyComplainantUpdate(Complainant complainant, UpdateCaseDTO dto) {
        if (complainant == null) throw new IllegalStateException("Complainant record missing from case");

        Person person = complainant.getPerson();
        if (person == null) throw new IllegalStateException("Person record missing from complainant");

        person.setLastName(dto.complainantLastName());
        person.setFirstName(dto.complainantFirstName());
        person.setMiddleName(dto.complainantMiddleName());
        person.setContactNumber(dto.complainantContact());
        person.setAge(dto.complainantAge() != null ? dto.complainantAge().shortValue() : null);
        person.setGender(dto.complainantGender());
        person.setCivilStatus(dto.complainantCivilStatus());
        person.setEmail(dto.complainantEmail());
        person.setCompleteAddress(dto.complainantAddress());
    }

    private void applyRespondentUpdate(Respondent respondent, UpdateCaseDTO dto) {
        if (respondent == null) throw new IllegalStateException("Respondent record missing from case");

        Person person = respondent.getPerson();
        if (person == null) throw new IllegalStateException("Person record missing from respondent");

        person.setLastName(dto.respondentLastName());
        person.setFirstName(dto.respondentFirstName());
        person.setMiddleName(dto.respondentMiddleName());
        person.setAge(dto.respondentAge());
        person.setBirthDate(dto.respondentDob());
        person.setGender(dto.respondentGender());
        person.setCivilStatus(dto.respondentCivilStatus());
        person.setContactNumber(dto.respondentContact());
        person.setCompleteAddress(dto.respondentAddress());

        respondent.setAlias(dto.respondentAlias());
        respondent.setRelationshipToComplainant(dto.relationshipTypeName());
        respondent.setLivingWithComplainant(dto.livingWithComplainant());
    }

    private void applyIncidentDetailUpdate(IncidentDetail detail, UpdateCaseDTO dto) {
        if (detail == null) throw new IllegalStateException("Incident detail record missing from case");


        detail.setDateOfIncident(dto.dateOfIncident());
        detail.setNatureOfComplaint(dto.natureOfComplaintId());
        detail.setTimeOfIncident(dto.timeOfIncident());
        detail.setPlaceOfIncident(dto.placeOfIncident());
        detail.setFrequency(dto.frequencyOfIncident());
        detail.setInjuriesDamagesDescription(dto.descriptionOfInjuries());
    }

    private void applyWitnessUpdate(BlotterCase blotterCase, List<WitnessDTO> witnessDTOs) {
        List<Witness> existing = blotterCase.getWitnesses();

        List<Long> orphanedPersonIds = existing.stream()
                .filter(w -> w.getPerson() != null && Boolean.FALSE.equals(w.getPerson().getIsResident()))
                .map(w -> w.getPerson().getId())
                .toList();

        witnessRepository.deleteAllByBlotterCase(blotterCase);
        existing.clear();

        if (!orphanedPersonIds.isEmpty()) {
            personRepository.deleteNonResidentsByIds(orphanedPersonIds);
        }

        if (witnessDTOs == null || witnessDTOs.isEmpty()) return;

        List<Witness> incoming = witnessDTOs.stream().map(dto -> {
            Person person;

            if (dto.personId() != null) {
                person = personRepository.findById(dto.personId())
                        .orElseThrow(() -> new RuntimeException("Witness " +  dto.personId() + " not found"));

            } else {
                person = new Person();
                person.setFirstName(dto.fullName());
                person.setLastName("");
                person.setContactNumber(dto.contactNumber());
                person.setCompleteAddress(dto.address());
                person.setIsResident(false);
                person = personRepository.save(person);
            }

            Witness witness = new Witness();
            witness.setBlotterCase(blotterCase);
            witness.setPerson(person);
            witness.setTestimony(dto.testimony());
            return witness;
        }).toList();

        List<Witness> saved = witnessRepository.saveAll(incoming);
        blotterCase.getWitnesses().addAll(saved);
    }

    private void applyEvidenceUpdate(BlotterCase blotterCase, List<String> evidenceTypeIds, User receivedBy) {
        evidenceRecordRepository.deleteAllByBlotterCase(blotterCase);

        if (evidenceTypeIds == null || evidenceTypeIds.isEmpty()) return;

        List<EvidenceRecord> records = evidenceTypeIds.stream().map(id -> {
            EvidenceType type = evidenceTypeRepository.findById(Long.parseLong(id))
                    .orElseThrow(() -> new RuntimeException("Evidence type not found: " + id));
            EvidenceRecord record = new EvidenceRecord();
            record.setBlotterCase(blotterCase);
            record.setType(type);
            record.setReceivedBy(receivedBy);
            return record;
        }).toList();

        evidenceRecordRepository.saveAll(records);
    }

    private void applyAssigneeUpdate(BlotterCase blotterCase, Long assignToId) {
        if (assignToId == null) {
            blotterCase.setEmployee(null);
            return;
        }

        Employee employee = employeeRepository.findById(assignToId)
                .orElseThrow(() -> new RuntimeException("Employee not found: " + assignToId));

        blotterCase.setEmployee(employee);


    }




}