package com.barangay.barangay.blotter.service;

import com.barangay.barangay.admin_management.model.User;
import com.barangay.barangay.audit.service.AuditLogService;
import com.barangay.barangay.blotter.constant.EvidenceConstants;
import com.barangay.barangay.blotter.constant.NatureOfComplaintConstants;
import com.barangay.barangay.blotter.dto.notes.AddCaseNoteRequest;
import com.barangay.barangay.blotter.dto.complaint.EvidenceOptionDTO;
import com.barangay.barangay.blotter.dto.complaint.NatureOptionDTO;
import com.barangay.barangay.blotter.dto.Records.FtrSummaryStatsDTO;
import com.barangay.barangay.blotter.dto.Records.UpdateStatusDTO;
import com.barangay.barangay.blotter.dto.reports_and_display.CaseTimeLineDTO;
import com.barangay.barangay.blotter.model.*;
import com.barangay.barangay.blotter.repository.*;
import com.barangay.barangay.department.model.Department;
import com.barangay.barangay.enumerated.*;
import com.barangay.barangay.user_management.repository.UserManagementRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
    private final HearingRepository hearingRepository;



    @Transactional
    public void addNoteToCase(AddCaseNoteRequest dto, User officer, String ipAddress) {
        User managedOfficer = userManagementRepository.findByIdWithDepartments(officer.getId())
                .orElseThrow(() -> new RuntimeException("Officer not found."));

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
        timeline.setTitle("Note Added");

        String noteSnippet = dto.note().length() > 100
                ? dto.note().substring(0, 97) + "..."
                : dto.note();
        timeline.setDescription(noteSnippet);

        timeline.setPerformedBy(managedOfficer);
        caseTimeLineRepository.save(timeline);


        //  AUDIT LOG
        logNoteActivity(managedOfficer, blotterCase, dto.note(), ipAddress);

    }


    @Transactional
    public void updateStatus(UpdateStatusDTO dto, User actor, String ipAddress) {
        BlotterCase blotter = blotterCaseRepository.findByBlotterNumber(dto.blotterNumber())
                .orElseThrow(() -> new RuntimeException("Case not found"));

        CaseStatus current = blotter.getStatus();
        CaseStatus next = dto.newStatus();

        validateTransition(current, next);

        blotter.setStatus(next);
        blotter.setStatusRemarks(dto.reason());

        blotterCaseRepository.save(blotter);

        TimelineEventType resolvedEventType = switch (next) {
            case SETTLED -> TimelineEventType.CASE_SETTLED;
            case DISMISSED -> TimelineEventType.CASE_DISMISSED;
            case REFERRED_TO_LUPON -> TimelineEventType.CASE_REFERRED;
            default -> TimelineEventType.STATUS_CHANGED;
        };

        CaseTimeline timeline = new CaseTimeline();
        timeline.setBlotterCase(blotter);
        timeline.setEventType(resolvedEventType);
        timeline.setTitle("Case Updated to " + next.name());
        timeline.setDescription("Reason: " + dto.reason());
        timeline.setPerformedBy(actor);

        caseTimeLineRepository.save(timeline);

        if (next == CaseStatus.SETTLED || next == CaseStatus.DISMISSED) {
            List<Hearing> pendingHearings = hearingRepository.findByBlotterCaseAndStatus(blotter, HearingStatus.SCHEDULED);

            if (!pendingHearings.isEmpty()) {
                for (Hearing hearing : pendingHearings) {
                    hearing.setStatus(HearingStatus.CANCELLED);
                    hearing.setNotes("Auto-cancelled because case status was updated to " + next.name());
                }
                hearingRepository.saveAll(pendingHearings);
            }
        }

        auditLogService.log(
                actor,
                Departments.BLOTTER,
                "BLOTTER_MANAGEMENT",
                Severity.INFO,
                "STATUS_CHANGE",
                ipAddress,
                dto.reason(),
                current.name(),
                next.name()
        );
    }
    private void validateTransition(CaseStatus current, CaseStatus next) {
        if (current == next) throw new RuntimeException("Status is already " + next);


        if (next == CaseStatus.ELEVATED_TO_FORMAL) {
            if (current == CaseStatus.RECORDED) {
                throw new RuntimeException("Cannot escalate: This case is already RECORDED and cannot be modified.");
            }
            return;
        }

        switch (current) {
            case PENDING -> {
                if (next != CaseStatus.UNDER_MEDIATION &&
                        next != CaseStatus.RECORDED &&
                        next != CaseStatus.DISMISSED &&
                        next != CaseStatus.ELEVATED_TO_FORMAL) {
                    throw new RuntimeException("Invalid transition from Pending.");
                }
            }
            case RECORDED, SETTLED, DISMISSED, CERTIFIED_TO_FILE_ACTION, CLOSED, ARCHIVED -> {
                throw new RuntimeException("This status is final and cannot be modified.");
            }
            case EXPIRED_UNACTIONED -> {
                if (next != CaseStatus.REFERRED_TO_LUPON && next != CaseStatus.DISMISSED) {
                    throw new RuntimeException("Case has expired.");
                }
            }
        }
    }



    private void logNoteActivity(User officer, BlotterCase bc, String note, String ip) {
        try {
            Map<String, Object> snapshot = new LinkedHashMap<>();
            snapshot.put("caseNumber", bc.getBlotterNumber());
            snapshot.put("noteSnippet", note.substring(0, Math.min(note.length(), 100)));
            snapshot.put("officer", officer.getPerson().getFirstName() + " " + officer.getPerson().getLastName());

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



    @Transactional(readOnly = true)
    public FtrSummaryStatsDTO getFtrDashboardStats(User officer) {
        Long deptId = officer.getAllowedDepartments().stream()
                .findFirst()
                .map(Department::getId)
                .orElseThrow(() -> new RuntimeException("Unauthorized: No department assigned."));

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startThisMonth = now.withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0);
        LocalDateTime startLastMonth = startThisMonth.minusMonths(1);

        // 1. Total FTR and Trend
        long curFtr = blotterCaseRepository.countFtrByType(deptId, CaseType.FOR_THE_RECORD, startThisMonth, now);
        long prevFtr = blotterCaseRepository.countFtrByType(deptId, CaseType.FOR_THE_RECORD, startLastMonth, startThisMonth.minusSeconds(1));

        // 2. Frequent Subjects (Suki)
        long frequentSubjects = blotterCaseRepository.countFrequentFtrSubjects(deptId, startThisMonth);

        // 3. Top Nature
        String topNature = blotterCaseRepository.findTopFtrNature(deptId, startThisMonth)
                .orElse("General Record");

        // 4. Peak Time Logic
        List<java.sql.Time> rawTimes = blotterCaseRepository.findFtrIncidentTimesRaw(deptId, startThisMonth);
        List<LocalTime> incidentTimes = rawTimes.stream().map(java.sql.Time::toLocalTime).collect(Collectors.toList());

        String peakShift = "No Data Yet";
        long maxCount = 0;

        if (!incidentTimes.isEmpty()) {
            long morning = 0, afternoon = 0, evening = 0, lateNight = 0;
            for (LocalTime time : incidentTimes) {
                int hour = time.getHour();
                if (hour >= 6 && hour < 12) morning++;
                else if (hour >= 12 && hour < 18) afternoon++;
                else if (hour >= 18 && hour < 23) evening++;
                else lateNight++;
            }
            maxCount = morning; peakShift = "Morning (6AM-12PM)";
            if (afternoon > maxCount) { maxCount = afternoon; peakShift = "Afternoon (12PM-6PM)"; }
            if (evening > maxCount) { maxCount = evening; peakShift = "Evening (6PM-12AM)"; }
            if (lateNight > maxCount) { maxCount = lateNight; peakShift = "Late Night (12AM-6AM)"; }
        }

        return new FtrSummaryStatsDTO(
                curFtr, calculateTrend(curFtr, prevFtr),
                frequentSubjects,
                topNature,
                peakShift, maxCount
        );
    }
    private double calculateTrend(long current, long previous) {
        if (previous == 0) return current > 0 ? 100.0 : 0.0;
        return ((double) (current - previous) / previous) * 100.0;
    }



    public List<CaseTimeLineDTO> getTimelineByCase(String caseId) {
        return caseTimeLineRepository.findByBlotterCase_BlotterNumberOrderByEventDateDesc(caseId)
                .stream()
                .map(t -> new CaseTimeLineDTO(
                        t.getId(),
                        t.getEventType(),
                        t.getTitle(),
                        t.getDescription(),
                        t.getPerformedBy() != null ?
                                t.getPerformedBy().getPerson().getFirstName() + " " + t.getPerformedBy().getPerson().getLastName() : "System",
                        t.getEventDate()
                ))
                .collect(Collectors.toList());
    }


}
