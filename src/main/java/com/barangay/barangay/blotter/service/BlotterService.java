package com.barangay.barangay.blotter.service;

import com.barangay.barangay.admin_management.model.User;
import com.barangay.barangay.audit.service.AuditLogService;
import com.barangay.barangay.blotter.constant.EvidenceConstants;
import com.barangay.barangay.blotter.constant.NatureOfComplaintConstants;
import com.barangay.barangay.blotter.dto.AddCaseNoteRequest;
import com.barangay.barangay.blotter.dto.EvidenceOptionDTO;
import com.barangay.barangay.blotter.dto.NatureOptionDTO;
import com.barangay.barangay.blotter.model.*;
import com.barangay.barangay.blotter.repository.*;
import com.barangay.barangay.enumerated.Departments;
import com.barangay.barangay.enumerated.Severity;
import com.barangay.barangay.enumerated.TimelineEventType;
import com.barangay.barangay.user_management.repository.UserManagementRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class BlotterService {

    private final CaseNoteRepository caseNoteRepository;
    private final UserManagementRepository userManagementRepository;
    private final BlotterCaseRepository blotterCaseRepository;
    private final ObjectMapper objectMapper;
    private final AuditLogService auditLogService;
    private final CasteTimeLineRepository caseTimeLineRepository;
    private final EvidenceTypeRepository  evidenceTypeRepository;
    private final NatureOfComplaintRepository natureOfComplaintRepository;



    @Transactional
    public void addNoteToCase(AddCaseNoteRequest dto, User officer, String ipAddress) {
        User managedOfficer = userManagementRepository.findByIdWithDepartments(officer.getId())
                .orElseThrow(() -> new RuntimeException("Officer not found."));
        validateOfficerAccess(managedOfficer);

        BlotterCase blotterCase = blotterCaseRepository.findByBlotterNumber(dto.blotterNumber())
                .orElseThrow(() -> new RuntimeException("Case not found: " + dto.blotterNumber()));

        CaseNote caseNote = new CaseNote();
        caseNote.setBlotterCase(blotterCase);
        caseNote.setNote(dto.note());
        caseNote.setCreatedBy(managedOfficer);

        caseNoteRepository.save(caseNote);


        CaseTimeline timeline = new CaseTimeline();
        timeline.setBlotterCase(blotterCase);

        timeline.setEventType(TimelineEventType.NOTE_ADDED);
        timeline.setTitle("New Follow-up Note Added");

        String noteSnippet = dto.note().length() > 100
                ? dto.note().substring(0, 97) + "..."
                : dto.note();
        timeline.setDescription(noteSnippet);

        timeline.setPerformedBy(managedOfficer);
        caseTimeLineRepository.save(timeline);


        //  AUDIT LOG
        logNoteActivity(managedOfficer, blotterCase, dto.note(), ipAddress);

    }


    private void validateOfficerAccess(User officer) {

        boolean isBlotterDept = officer.getAllowedDepartments().stream()
                .anyMatch(d -> d.getName().equalsIgnoreCase("BLOTTER") || d.getId() == 3L);

        boolean hasCreatePerm = officer.getCustomPermissions().stream()
                .anyMatch(p -> p.getPermissionName().equalsIgnoreCase("Create Records"));

        if (!isBlotterDept || !hasCreatePerm) {
            throw new RuntimeException("Unauthorized: Access denied. User must be in the Blotter Department with 'Create Records' permission..");
        }
    }

    private void logNoteActivity(User officer, BlotterCase bc, String note, String ip) {
        try {
            Map<String, Object> snapshot = new LinkedHashMap<>();
            snapshot.put("caseNumber", bc.getBlotterNumber());
            snapshot.put("noteSnippet", note.substring(0, Math.min(note.length(), 100)));
            snapshot.put("officer", officer.getFirstName() + " " + officer.getLastName());

            String jsonState = objectMapper.writeValueAsString(snapshot);

            auditLogService.log(
                    officer,
                    Departments.BLOTTER,
                    "CASE_NOTE_ADDED",
                    Severity.INFO,
                    "ADD_NOTE",
                    ip,
                    "Added follow-up note to Case: " + bc.getBlotterNumber(),
                    null,
                    jsonState
            );
        } catch (Exception e) {
            auditLogService.log(officer, null, "ERROR", Severity.CRITICAL, "LOG_FAIL", ip, e.getMessage(), null, null);
        }
    }



    public List<EvidenceOptionDTO> getEvidenceOptions() {
        return evidenceTypeRepository.findByTypeNameInOrderByTypeNameAsc(EvidenceConstants.VALID_EVIDENCE_NAMES)
                .stream()
                .map(type -> new EvidenceOptionDTO(
                        type.getId(),
                        type.getTypeName()
                ))
                .toList();
    }

    public List<NatureOptionDTO> getNatureOptions() {
        return natureOfComplaintRepository.findByNameInOrderByNameAsc(NatureOfComplaintConstants.VALID_NATURE_NAMES)
                .stream()
                .map(nature -> new NatureOptionDTO(
                        nature.getId(),
                        nature.getName()
                ))
                .toList();
    }



}
