package com.barangay.barangay.blotter.service;

import com.barangay.barangay.admin_management.model.User;
import com.barangay.barangay.audit.service.AuditLogService;
import com.barangay.barangay.blotter.dto.RecordMinutesRequest;
import com.barangay.barangay.blotter.dto.ScheduleHearingRequest;
import com.barangay.barangay.blotter.model.BlotterCase;
import com.barangay.barangay.blotter.model.CaseTimeline;
import com.barangay.barangay.blotter.model.Hearing;
import com.barangay.barangay.blotter.model.HearingMinutes;
import com.barangay.barangay.blotter.repository.BlotterCaseRepository;
import com.barangay.barangay.blotter.repository.CasteTimeLineRepository;
import com.barangay.barangay.blotter.repository.HearingMinutesRepository;
import com.barangay.barangay.blotter.repository.HearingRepository;
import com.barangay.barangay.enumerated.*;
import com.barangay.barangay.user_management.repository.UserManagementRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class HearingService {

    private final HearingRepository hearingRepository;
    private final BlotterCaseRepository blotterCaseRepository;
    private final UserManagementRepository userManagementRepository;
    private final AuditLogService auditLogService;
    private final HearingMinutesRepository hearingMinutesRepository;
    private final ObjectMapper objectMapper;
    private final CasteTimeLineRepository caseTimeLineRepository;





    @Transactional
    public void scheduleNewHearing(ScheduleHearingRequest dto, User officer, String ipAddress) {

        User managedOfficer = userManagementRepository.findByIdWithDepartments(officer.getId())
                .orElseThrow(() -> new RuntimeException("Officer session invalid."));

        validateOfficerAccess(managedOfficer);

        BlotterCase blotterCase = blotterCaseRepository.findByBlotterNumber(dto.blotterNumber())
                .orElseThrow(() -> new RuntimeException("Case not found: " + dto.blotterNumber()));

        if (dto.scheduledEnd().isBefore(dto.scheduledStart()) || dto.scheduledEnd().isEqual(dto.scheduledStart())) {
            throw new RuntimeException("Invalid schedule: The end time cannot be earlier than or equal to the start time.");
        }

        if (hearingRepository.existsOverlapping(dto.venue(), dto.scheduledStart(), dto.scheduledEnd())) {
            throw new RuntimeException("Scheduling conflict: The venue '" + dto.venue() + "' is already occupied during the specified time range.");
        }

        Short nextSummon = (short) (hearingRepository.findLastSummonNumber(blotterCase.getId()) + 1);

        Hearing hearing = new Hearing();
        hearing.setBlotterCase(blotterCase);
        hearing.setSummonNumber(nextSummon);
        hearing.setScheduledStart(dto.scheduledStart());
        hearing.setScheduledEnd(dto.scheduledEnd());
        hearing.setVenue(dto.venue());
        hearing.setNotes(dto.notes());
        hearing.setCreatedBy(managedOfficer);
        hearingRepository.save(hearing);

        if (blotterCase.getStatus() == CaseStatus.PENDING) {
            blotterCase.setStatus(CaseStatus.UNDER_MEDIATION);
            blotterCaseRepository.save(blotterCase);
        }


        CaseTimeline timeline = new CaseTimeline();
        timeline.setBlotterCase(blotterCase);
        timeline.setEventType(TimelineEventType.SUMMON_ISSUED); //
        timeline.setTitle("Hearing " + hearing.getSummonNumber() + " Issued");
        timeline.setDescription("Hearing scheduled on " +
                hearing.getScheduledStart().toLocalDate() + " at " + hearing.getVenue());
        timeline.setPerformedBy(managedOfficer);
        caseTimeLineRepository.save(timeline);

        logHearingActivity(managedOfficer, blotterCase, hearing, ipAddress);
    }





    private void validateOfficerAccess(User officer) {
        boolean isBlotterDept = officer.getAllowedDepartments().stream()
                .anyMatch(d -> d.getName().equalsIgnoreCase("BLOTTER") || d.getId() == 3L);

        boolean hasCreatePerm = officer.getCustomPermissions().stream()
                .anyMatch(p -> p.getPermissionName().equalsIgnoreCase("Create Records"));

        if (!isBlotterDept || !hasCreatePerm) {
            throw new RuntimeException("Access Denied: You don't have permission to schedule hearings.");
        }
    }

    private void logHearingActivity(User officer, BlotterCase bc, Hearing h, String ip) {
        try {
            Map<String, Object> snapshot = new LinkedHashMap<>();
            snapshot.put("Case Number", bc.getBlotterNumber());
            snapshot.put("Summon Number", "Summon #" + h.getSummonNumber());

            String timeRange = String.format("%s - %s",
                    h.getScheduledStart().toLocalTime(),
                    h.getScheduledEnd().toLocalTime());

            snapshot.put("Hearing Date", h.getScheduledStart().toLocalDate().toString());
            snapshot.put("Time Slot", timeRange);
            snapshot.put("Venue", h.getVenue());

            String jsonState = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(snapshot);

            auditLogService.log(
                    officer,
                    Departments.BLOTTER,
                    "HEARING_SCHEDULED",
                    Severity.INFO,
                    "SCHEDULE_HEARING",
                    ip,
                    "Scheduled " + snapshot.get("Summon Number") + " for Case " + bc.getBlotterNumber(),
                    null,
                    jsonState
            );
        } catch (Exception e) {
            auditLogService.log(officer, null, "ERROR", Severity.CRITICAL, "LOG_FAIL", ip, "Audit log failed: " + e.getMessage(), null, null);
        }
    }



    @Transactional
    public void recordHearingMinutes(RecordMinutesRequest dto, User officer, String ipAddress) {
        User managedOfficer = userManagementRepository.findByIdWithDepartments(officer.getId())
                .orElseThrow(() -> new RuntimeException("Officer session invalid."));

        validateOfficerAccess(managedOfficer);

        Hearing hearing = hearingRepository.findById(dto.hearingId())
                .orElseThrow(() -> new RuntimeException("Hearing not found."));

        if (hearingMinutesRepository.existsByHearingId(dto.hearingId())) {
            throw new RuntimeException("Minutes have already been recorded for this hearing.");
        }

        HearingMinutes minutes = new HearingMinutes();
        minutes.setHearing(hearing);
        minutes.setComplainantPresent(dto.complainantPresent());
        minutes.setRespondentPresent(dto.respondentPresent());
        minutes.setHearingNotes(dto.hearingNotes());
        minutes.setOutcome(dto.outcome());
        minutes.setRecordedBy(managedOfficer);
        hearingMinutesRepository.save(minutes);

        // UPDATE HEARING STATUS
        hearing.setStatus(HearingStatus.COMPLETED);
        hearingRepository.save(hearing);

        BlotterCase bc = hearing.getBlotterCase();
        if (dto.outcome() == HearingOutcome.SETTLED) {
            bc.setStatus(CaseStatus.SETTLED);
        } else if (dto.outcome() == HearingOutcome.NOT_SETTLED) {
            bc.setStatus(CaseStatus.UNDER_MEDIATION);
        }
        blotterCaseRepository.save(bc);


        CaseTimeline timeline = new CaseTimeline();
        timeline.setBlotterCase(hearing.getBlotterCase());
        timeline.setEventType(TimelineEventType.HEARING_CONDUCTED);

        timeline.setTitle("Hearing " + hearing.getSummonNumber() + " Result: " + dto.outcome());

        String attendance = String.format("Attendance: Complainant (%s), Respondent (%s). ",
                dto.complainantPresent() ? "Present" : "Absent",
                dto.respondentPresent() ? "Present" : "Absent");

        timeline.setDescription(attendance + "Summary: " + dto.hearingNotes());
        timeline.setPerformedBy(officer);
        caseTimeLineRepository.save(timeline);

        logMinutesActivity(managedOfficer, bc, hearing, dto.outcome(), ipAddress);
    }

    private void logMinutesActivity(User officer, BlotterCase bc, Hearing h, HearingOutcome outcome, String ip) {
        try {
            Map<String, Object> snapshot = new LinkedHashMap<>();
            snapshot.put("Case Number", bc.getBlotterNumber());
            snapshot.put("Summon Number", "Patawag #" + h.getSummonNumber());
            snapshot.put("Hearing Date", h.getScheduledStart().toLocalDate().toString());
            snapshot.put("Outcome", outcome.toString().replace("_", " "));
            snapshot.put("Officer In-Charge", officer.getFirstName() + " " + officer.getLastName());

            String jsonState = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(snapshot);

            auditLogService.log(
                    officer,
                    Departments.BLOTTER,
                    "HEARING_MINUTES_RECORDED",
                    Severity.INFO,
                    "RECORD_MINUTES",
                    ip,
                    "Recorded " + outcome + " for " + snapshot.get("Summon Number") + " (Case: " + bc.getBlotterNumber() + ")",
                    null,
                    jsonState
            );
        } catch (Exception e) {
            auditLogService.log(officer, null, "ERROR", Severity.CRITICAL, "LOG_FAIL", ip, "Failed to log minutes: " + e.getMessage(), null, null);
        }
    }
}

