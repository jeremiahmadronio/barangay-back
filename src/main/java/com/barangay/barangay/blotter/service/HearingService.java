package com.barangay.barangay.blotter.service;

import com.barangay.barangay.admin_management.model.User;
import com.barangay.barangay.audit.service.AuditLogService;
import com.barangay.barangay.blotter.dto.hearing.*;
import com.barangay.barangay.blotter.model.*;
import com.barangay.barangay.blotter.repository.*;
import com.barangay.barangay.enumerated.*;
import com.barangay.barangay.user_management.repository.UserManagementRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
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
    private final FollowUpHearingRepository followUpHearingRepository;





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
        timeline.setTitle("Mediation " + hearing.getSummonNumber() + " Issued");
        timeline.setDescription("Mediation   scheduled on " +
                hearing.getScheduledStart().toLocalDate() + " at " + hearing.getVenue());
        timeline.setPerformedBy(managedOfficer);
        caseTimeLineRepository.save(timeline);

        logHearingActivity(managedOfficer, blotterCase, hearing, ipAddress);
    }






    @Transactional
    public void recordHearingFollowUp(Long hearingId, FollowUpHearingDTO dto, User officer, String ipAddress) {
        User managedOfficer = userManagementRepository.findByIdWithDepartments(officer.getId())
                .orElseThrow(() -> new RuntimeException("Officer session invalid."));
        validateOfficerAccess(managedOfficer);

        Hearing hearing = hearingRepository.findById(hearingId)
                .orElseThrow(() -> new RuntimeException("Mediation not found."));

        HearingFollowUp followUp = new HearingFollowUp();
        followUp.setHearing(hearing);
        followUp.setRemarks(dto.notes());
        followUp.setRecordedBy(managedOfficer);
        followUpHearingRepository.save(followUp);

        CaseTimeline timeline = new CaseTimeline();
        timeline.setBlotterCase(hearing.getBlotterCase());
        timeline.setEventType(TimelineEventType.HEARING_FOLLOWUP);
        timeline.setTitle("Follow-up added for Mediation #" + hearing.getSummonNumber());
        timeline.setDescription("Mediation follow up " + dto.notes());
        timeline.setPerformedBy(managedOfficer);
        caseTimeLineRepository.save(timeline);

        logFollowUpActivity(managedOfficer, hearing.getBlotterCase(), hearing, dto, ipAddress);
    }


    private void logFollowUpActivity(User officer, BlotterCase bc, Hearing h, FollowUpHearingDTO dto, String ip) {
        try {
            Map<String, Object> snapshot = new LinkedHashMap<>();
            snapshot.put("Case Number", bc.getBlotterNumber());
            snapshot.put("Mediation Number", "Mediation #" + h.getSummonNumber());
            snapshot.put("Remarks", dto.notes());
            snapshot.put("Added By", officer.getFirstName() + " " + officer.getLastName());

            String jsonState = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(snapshot);

            auditLogService.log(
                    officer,
                    Departments.BLOTTER,
                    "MEDIATION_FOLLOW_UP_ADDED",
                    Severity.INFO,
                    "ADD_FOLLOW_UP",
                    ip,
                    "Added follow-up notes for Mediation #" + h.getSummonNumber() + " (Case: " + bc.getBlotterNumber() + ")",
                    null,
                    jsonState
            );
        } catch (Exception e) {
            auditLogService.log(officer, null, "ERROR", Severity.CRITICAL, "LOG_FAIL", ip, "Follow-up audit log failed: " + e.getMessage(), null, null);
        }
    }







    @Transactional(readOnly = true)
    public HearingFullDetailsDTO getHearingFullDetails(Long hearingId) {
        Hearing hearing = hearingRepository.findById(hearingId)
                .orElseThrow(() -> new RuntimeException("Hearing not found."));

        HearingMinutes minutes = hearingMinutesRepository.findByHearingId(hearingId).orElse(null);

        MinutesSummaryDTO minutesDto = (minutes == null) ? null : new MinutesSummaryDTO(
                minutes.getComplainantPresent(),
                minutes.getRespondentPresent(),
                minutes.getHearingNotes(),
                minutes.getOutcome(),
                minutes.getRecordedBy().getFirstName() + " " + minutes.getRecordedBy().getLastName()
        );

        List<FollowUpSummaryDTO> followUps = hearing.getFollowUps().stream()
                .map(f -> new FollowUpSummaryDTO(
                        f.getId(),
                        f.getRemarks(),
                        f.getRecordedBy().getFirstName() + " " + f.getRecordedBy().getLastName(),
                        f.getCreatedAt()
                )).toList();

        return new HearingFullDetailsDTO(
                hearing.getId(),
                hearing.getSummonNumber(),
                hearing.getStatus(),
                hearing.getScheduledStart(),
                hearing.getVenue(),
                hearing.getNotes(),
                minutesDto,
                followUps
        );
    }




    private void validateOfficerAccess(User officer) {
        boolean isBlotterDept = officer.getAllowedDepartments().stream()
                .anyMatch(d -> d.getName().equalsIgnoreCase("BLOTTER") || d.getId() == 3L);

        boolean hasCreatePerm = officer.getCustomPermissions().stream()
                .anyMatch(p -> p.getPermissionName().equalsIgnoreCase("Manage Hearings & Mediation"));

        if (!isBlotterDept || !hasCreatePerm) {
            throw new RuntimeException("Access Denied: You don't have permission to schedule hearings.");
        }
    }

    private void logHearingActivity(User officer, BlotterCase bc, Hearing h, String ip) {
        try {
            Map<String, Object> snapshot = new LinkedHashMap<>();
            snapshot.put("Case Number", bc.getBlotterNumber());
            snapshot.put("Mediation Number", "Mediation #" + h.getSummonNumber());

            String timeRange = String.format("%s - %s",
                    h.getScheduledStart().toLocalTime(),
                    h.getScheduledEnd().toLocalTime());

            snapshot.put("Mediation Date", h.getScheduledStart().toLocalDate().toString());
            snapshot.put("Time Slot", timeRange);
            snapshot.put("Venue", h.getVenue());

            String jsonState = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(snapshot);

            auditLogService.log(
                    officer,
                    Departments.BLOTTER,
                    "MEDIATION_SCHEDULED",
                    Severity.INFO,
                    "SCHEDULE_MEDIATION",
                    ip,
                    "Scheduled " + snapshot.get("Mediation Number") + " for Case " + bc.getBlotterNumber(),
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

        // --- 1. SAVE HEARING MINUTES ---
        HearingMinutes minutes = new HearingMinutes();
        minutes.setHearing(hearing);
        minutes.setComplainantPresent(dto.complainantPresent());
        minutes.setRespondentPresent(dto.respondentPresent());
        minutes.setHearingNotes(dto.hearingNotes());
        minutes.setOutcome(dto.outcome());
        minutes.setRecordedBy(managedOfficer);
        hearingMinutesRepository.save(minutes);

        // --- 2. UPDATE HEARING STATUS ---
        hearing.setStatus(HearingStatus.COMPLETED);
        hearingRepository.save(hearing);

        BlotterCase bc = hearing.getBlotterCase();
        TimelineEventType eventType = TimelineEventType.HEARING_CONDUCTED; // Default
        String additionalTimelineDesc = "";

        if (dto.outcome() == HearingOutcome.SETTLED) {
            if (dto.settlementTerms() == null || dto.settlementTerms().isBlank()) {
                throw new IllegalArgumentException("Settlement agreement terms must be provided when outcome is SETTLED.");
            }

            bc.setStatus(CaseStatus.SETTLED);
            bc.setSettlementTerms(dto.settlementTerms());
            bc.setSettledAt(LocalDateTime.now());
            eventType = TimelineEventType.CASE_SETTLED; // Ibahin natin ang event type para exact sa timeline
            additionalTimelineDesc = " | Agreement: " + dto.settlementTerms();

            // WAG KALIMUTAN ITO: I-cancel ang mga naka-schedule pang hearing para sa kasong ito
            List<Hearing> pendingHearings = hearingRepository.findByBlotterCaseAndStatus(bc, HearingStatus.SCHEDULED);
            if (!pendingHearings.isEmpty()) {
                for (Hearing h : pendingHearings) {
                    h.setStatus(HearingStatus.CANCELLED);
                    h.setNotes("Auto-cancelled because case was settled in Hearing ID: " + hearing.getId());
                }
                hearingRepository.saveAll(pendingHearings);
            }

        } else if (dto.outcome() == HearingOutcome.NOT_SETTLED) {
            bc.setStatus(CaseStatus.UNDER_MEDIATION);
        }
        blotterCaseRepository.save(bc);

        // --- 4. CREATE TIMELINE RECORD ---
        CaseTimeline timeline = new CaseTimeline();
        timeline.setBlotterCase(bc);
        timeline.setEventType(eventType);
        timeline.setTitle("Mediation " + hearing.getSummonNumber() + " Result: " + dto.outcome());

        String attendance = String.format("Attendance: Complainant (%s), Respondent (%s). ",
                dto.complainantPresent() ? "Present" : "Absent",
                dto.respondentPresent() ? "Present" : "Absent");

        timeline.setDescription(attendance + "Summary: " + dto.hearingNotes() + additionalTimelineDesc);
        timeline.setPerformedBy(managedOfficer);
        caseTimeLineRepository.save(timeline); // Tiningnan ko yung code mo, "caseTimeLineRepository" ang na-type mo, make sure tama ang case (Timeline vs TimeLine)

        // --- 5. LOG ACTIVITY ---
        logMinutesActivity(managedOfficer, bc, hearing, dto.outcome(), ipAddress);
    }

    private void logMinutesActivity(User officer, BlotterCase bc, Hearing h, HearingOutcome outcome, String ip) {
        try {
            Map<String, Object> snapshot = new LinkedHashMap<>();
            snapshot.put("Case Number", bc.getBlotterNumber());
            snapshot.put("Mediation Number", "Patawag #" + h.getSummonNumber());
            snapshot.put("Mediation Date", h.getScheduledStart().toLocalDate().toString());
            snapshot.put("Outcome", outcome.toString().replace("_", " "));
            snapshot.put("Officer In-Charge", officer.getFirstName() + " " + officer.getLastName());

            String jsonState = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(snapshot);

            auditLogService.log(
                    officer,
                    Departments.BLOTTER,
                    "MEDIATION_MINUTES_RECORDED",
                    Severity.INFO,
                    "RECORD_MINUTES",
                    ip,
                    "Recorded " + outcome + " for " + snapshot.get("mediation Number") + " (Case: " + bc.getBlotterNumber() + ")",
                    null,
                    jsonState
            );
        } catch (Exception e) {
            auditLogService.log(officer, null, "ERROR", Severity.CRITICAL, "LOG_FAIL", ip, "Failed to log minutes: " + e.getMessage(), null, null);
        }
    }
}

