package com.barangay.barangay.vawc.service;

import com.barangay.barangay.admin_management.model.User;
import com.barangay.barangay.audit.service.AuditLogService;
import com.barangay.barangay.blotter.constant.EvidenceConstants;
import com.barangay.barangay.blotter.dto.complaint.EvidenceOptionDTO;
import com.barangay.barangay.blotter.dto.complaint.RecordBlotterEntry;
import com.barangay.barangay.blotter.dto.complaint.WitnessDTO;
import com.barangay.barangay.blotter.model.*;
import com.barangay.barangay.blotter.model.EvidenceType;
import com.barangay.barangay.blotter.repository.BlotterCaseRepository;
import com.barangay.barangay.blotter.repository.CasteTimeLineRepository;
import com.barangay.barangay.blotter.repository.EvidenceRecordRepository;
import com.barangay.barangay.blotter.repository.EvidenceTypeRepository;
import com.barangay.barangay.blotter.service.BlotterService;
import com.barangay.barangay.department.model.Department;
import com.barangay.barangay.employee.model.Employee;
import com.barangay.barangay.employee.repository.EmployeeRepository;
import com.barangay.barangay.enumerated.*;
import com.barangay.barangay.person.model.Complainant;
import com.barangay.barangay.person.model.Person;
import com.barangay.barangay.person.model.Respondent;
import com.barangay.barangay.person.model.Witness;
import com.barangay.barangay.person.repository.PersonRepository;
import com.barangay.barangay.user_management.repository.UserManagementRepository;
import com.barangay.barangay.vawc.dto.ComplaintDTO;
import com.barangay.barangay.vawc.dto.ViolenceOptionDTO;
import com.barangay.barangay.vawc.model.BaranggayProtectionOrder;
import com.barangay.barangay.vawc.model.ViolenceType;
import com.barangay.barangay.vawc.repository.BarangayProtectionOrderRepository;
import com.barangay.barangay.vawc.repository.ViolenceTypeRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ComplaintService {

    private final BlotterCaseRepository blotterCaseRepository;
    private final PersonRepository personRepository;
    private final ViolenceTypeRepository violenceTypeRepository;
    private final EvidenceTypeRepository evidenceTypeRepository;
    private final CasteTimeLineRepository caseTimeLineRepository;
    private final UserManagementRepository userManagementRepository;
    private final AuditLogService auditLogService;
    private final EmployeeRepository employeeRepository;
    private final EvidenceRecordRepository evidenceRecordRepository;
    private final ObjectMapper  objectMapper;
    private final BarangayProtectionOrderRepository barangayProtectionOrderRepository;




    @Transactional
    public String fileVAWCComplaint(ComplaintDTO dto, User officer, String ipAddress) {

        User managedOfficer = userManagementRepository.findById(officer.getId())
                .orElseThrow(() -> new RuntimeException("Officer not found."));

        Department userDept = managedOfficer.getAllowedDepartments().stream()
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Officer has no assigned department."));

        Person complainantPerson = getOrSavePerson(
                dto.complainantId(),
                dto.complainantFirstName(),
                dto.complainantLastName(),
                dto.complainantMiddleName(),
                dto.complainantContact(),
                dto.complainantAddress(),
                dto.complainantGender(),
                null
        );

        // 3. RESPONDENT — same logic
        Person respondentPerson = getOrSavePerson(
                dto.respondentId(),
                dto.respondentFirstName(),
                dto.respondentLastName(),
                dto.respondentMiddleName(),
                dto.respondentContact(),
                dto.respondentAddress(),
                dto.respondentGender(),
                dto.respondentDob()
        );

        String natureOfComplaint = "VAWC";
        if (dto.violenceTypeIds() != null && !dto.violenceTypeIds().isEmpty()) {
            List<ViolenceType> violenceTypes = violenceTypeRepository.findAllByIdIn(dto.violenceTypeIds());
            if (violenceTypes.isEmpty()) {
                throw new RuntimeException("Invalid violence type IDs provided.");
            }
            natureOfComplaint = violenceTypes.stream()
                    .map(ViolenceType::getName)
                    .collect(Collectors.joining(","));
        }

        BlotterCase blotter = new BlotterCase();
        blotter.setBlotterNumber(generateCaseNumber());
        blotter.setCaseType(CaseType.FORMAL_COMPLAINT);
        blotter.setStatus(CaseStatus.PENDING);
        blotter.setDepartment(userDept);
        blotter.setCreatedBy(managedOfficer);
        blotter.setIsCertified(true);
        blotter.setCertifiedAt(LocalDateTime.now());

        Complainant cLink = new Complainant();
        cLink.setPerson(complainantPerson);
        blotter.setComplainant(cLink);

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
        incident.setNatureOfComplaint(natureOfComplaint);
        incident.setDateOfIncident(dto.dateOfIncident());
        incident.setTimeOfIncident(dto.timeOfIncident());
        incident.setPlaceOfIncident(dto.placeOfIncident());
        incident.setInjuriesDamagesDescription(dto.descriptionOfInjuries());
        if (dto.frequencyOfIncident() != null) {
            incident.setFrequency(dto.frequencyOfIncident());
        }
        blotter.setIncidentDetail(incident);

        if (dto.witnesses() != null) {
            for (WitnessDTO wDto : dto.witnesses()) {
                Person witnessPerson = getOrSavePerson(
                        wDto.personId(),
                        wDto.fullName(),
                        "Witness",
                        "",
                        wDto.contactNumber(),
                        wDto.address(),
                        null,
                        null
                );
                Witness witness = new Witness();
                witness.setPerson(witnessPerson);
                witness.setTestimony(wDto.testimony());
                witness.setBlotterCase(blotter);
                blotter.getWitnesses().add(witness);
            }
        }

        if (dto.assignToId() != null) {
            Employee assignedEmployee = employeeRepository.findById(dto.assignToId())
                    .orElseThrow(() -> new RuntimeException("Employee not found."));
            if (!assignedEmployee.getIsActive()) {
                throw new RuntimeException("Assigned officer is inactive.");
            }
            blotter.setEmployee(assignedEmployee);
        }

        BlotterCase savedCase = blotterCaseRepository.save(blotter);

        if (dto.evidenceTypeIds() != null && !dto.evidenceTypeIds().isEmpty()) {
            List<Long> parsedIds = dto.evidenceTypeIds().stream()
                    .map(Long::parseLong)
                    .toList();
            List<EvidenceType> evidenceTypes = evidenceTypeRepository.findAllByIdIn(parsedIds);
            List<EvidenceRecord> records = evidenceTypes.stream().map(et -> {
                EvidenceRecord er = new EvidenceRecord();
                er.setBlotterCase(savedCase);
                er.setType(et);
                er.setReceivedBy(managedOfficer);
                return er;
            }).toList();
             evidenceRecordRepository.saveAll(records);
        }

        BaranggayProtectionOrder bpo = new BaranggayProtectionOrder();
        bpo.setBlotterCase(savedCase);
        bpo.setBpoControlNumber("BPO-" + savedCase.getBlotterNumber());
        bpo.setStatus(BpoStatus.PENDING);
        bpo.setCreatedBy(managedOfficer);



        if (dto.violenceTypeIds() != null && !dto.violenceTypeIds().isEmpty()) {
            List<ViolenceType> selectedViolenceTypes = violenceTypeRepository.findAllById(dto.violenceTypeIds());
            bpo.setViolenceTypes(new HashSet<>(selectedViolenceTypes));
        }

        barangayProtectionOrderRepository.save(bpo);


        CaseTimeline timeline = new CaseTimeline();
        timeline.setBlotterCase(savedCase);
        timeline.setEventType(TimelineEventType.CASE_FILED);
        timeline.setTitle("VAWC Case Filed");
        timeline.setDescription("Filed by officer: " + managedOfficer.getPerson().getLastName());
        timeline.setPerformedBy(managedOfficer);
        caseTimeLineRepository.save(timeline);

        logDetailedActivity(managedOfficer, savedCase, complainantPerson, dto, ipAddress);

        return savedCase.getBlotterNumber();

    }

    private Person getOrSavePerson(Long id, String first, String last,
                                   String middle, String contact,
                                   String address, String gender, LocalDate dob) {
        if (id != null && id != 0L) {
            return personRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Person with ID " + id + " not found."));
        }

        if (first == null || first.isBlank()) {
            throw new RuntimeException("First name is required for manual entry.");
        }

        Person newPerson = new Person();
        newPerson.setFirstName(first);
        newPerson.setLastName(last != null ? last : "Unidentified");
        newPerson.setMiddleName(middle);
        newPerson.setContactNumber(contact);
        newPerson.setCompleteAddress(address);
        newPerson.setGender(gender);
        newPerson.setBirthDate(dob);
        newPerson.setIsResident(false);

        return personRepository.save(newPerson);



    }





    private String generateCaseNumber() {
        return LocalDateTime.now().getYear() + "-VWC-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }



    private void logDetailedActivity(User officer, BlotterCase bc, Person p, ComplaintDTO dto, String ip) {
        try {
            Map<String, Object> snapshot = new LinkedHashMap<>();
            snapshot.put("Case Number", bc.getBlotterNumber());
            snapshot.put("Complainant", p.getFirstName() + " " + p.getLastName());
            snapshot.put("Respondent", dto.respondentFirstName() + " " + dto.respondentLastName());
            snapshot.put("Incident Date", dto.dateOfIncident());

            // Safe Narrative Snippet
            String snippet = dto.narrativeStatement();
            if (snippet != null && snippet.length() > 80) {
                snippet = snippet.substring(0, 80) + "...";
            }
            snapshot.put("Narrative Snippet", snippet);

            // Convert Map to JSON String
            String jsonState = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(snapshot);

            // Call your Audit Log Service
            auditLogService.log(
                    officer,
                    Departments.VAWC, // Siguraduhin mong defined ito sa enum mo
                    "VAWC_MODULE",
                    Severity.INFO,
                    "CREATE_VAWC_COMPLAINT",
                    ip,
                    "Successfully filed VAWC complaint: " + bc.getBlotterNumber(),
                    null, // No old value for new records
                    jsonState
            );
        } catch (Exception e) {
            // Fallback log kung sumabog ang JSON processing
            auditLogService.log(officer, null, "ERROR", Severity.CRITICAL, "AUDIT_LOG_FAIL", ip, e.getMessage(), null, null);
        }
    }





    public  List<EvidenceOptionDTO> getEvidenceOptions() {
        return evidenceTypeRepository.findByTypeNameInOrderByTypeNameAsc(EvidenceConstants.VALID_EVIDENCE_NAMES)
                .stream()
                .map(type -> new EvidenceOptionDTO(
                        type.getId(),
                        type.getTypeName()
                ))
                .toList();
    }


    @Transactional(readOnly = true)
    public List<ViolenceOptionDTO> getViolenceOptions() {
        return violenceTypeRepository.findAll().stream()
                .map(v -> new ViolenceOptionDTO(
                        v.getId(),
                        v.getName()
                ))
                .sorted((a, b) -> a.type().compareToIgnoreCase(b.type()))
                .toList();
    }






}

