package com.barangay.barangay.blotter.service;

import com.barangay.barangay.admin_management.model.User;
import com.barangay.barangay.audit.service.AuditLogService;
import com.barangay.barangay.blotter.dto.hearing.*;
import com.barangay.barangay.blotter.model.*;
import com.barangay.barangay.blotter.repository.*;
import com.barangay.barangay.enumerated.*;
import com.barangay.barangay.user_management.repository.UserManagementRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class    HearingService {

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
        // 1. Fetch Officer and Validate Access
        User managedOfficer = userManagementRepository.findByIdWithDepartments(officer.getId())
                .orElseThrow(() -> new RuntimeException("Officer session invalid."));
        validateOfficerAccess(managedOfficer);

        // 2. Fetch Case
        BlotterCase blotterCase = blotterCaseRepository.findByBlotterNumber(dto.blotterNumber())
                .orElseThrow(() -> new RuntimeException("Case not found: " + dto.blotterNumber()));

        boolean isLupon = blotterCase.getDepartment() != null &&
                "LUPONG_TAGAPAMAYAPA".equalsIgnoreCase(blotterCase.getDepartment().getName());
        String stageLabel = isLupon ? "Conciliation" : "Mediation";

        if (!dto.scheduledEnd().isAfter(dto.scheduledStart())) {
            throw new RuntimeException("Invalid schedule: End time must be after start time.");
        }

        if (hearingRepository.existsActiveConflict(dto.venue(), dto.scheduledStart(), dto.scheduledEnd())) {
            throw new RuntimeException("Conflict: The venue '" + dto.venue() + "' has an existing SCHEDULED session at this time.");
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
        hearing.setStatus(HearingStatus.SCHEDULED);
        hearingRepository.save(hearing);

        String oldStatus = blotterCase.getStatus().toString();
        if (blotterCase.getStatus() == CaseStatus.PENDING) {
            blotterCase.setStatus(CaseStatus.UNDER_MEDIATION);
            blotterCaseRepository.save(blotterCase);
        }
        String newStatus = blotterCase.getStatus().toString();

        CaseTimeline timeline = new CaseTimeline();
        timeline.setBlotterCase(blotterCase);
        timeline.setEventType(TimelineEventType.SUMMON_ISSUED);
        timeline.setTitle(stageLabel + " #" + hearing.getSummonNumber() + " Issued");
        timeline.setDescription(stageLabel + " session scheduled on " +
                hearing.getScheduledStart().toLocalDate() + " at " + hearing.getVenue());
        timeline.setPerformedBy(managedOfficer);
        caseTimeLineRepository.save(timeline);

        auditLogService.log(
                managedOfficer,
                isLupon ? Departments.LUPONG_TAGAPAMAYAPA : Departments.BLOTTER,
                "SCHEDULE_NEW_" + stageLabel.toUpperCase(),
                Severity.INFO,
                "Scheduled " + stageLabel + " #" + hearing.getSummonNumber() + " for Case " + blotterCase.getBlotterNumber(),
                ipAddress,
                "Venue: " + hearing.getVenue(),
                "Old Case Status: " + oldStatus,
                "New Case Status: " + newStatus
        );
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
        timeline.setTitle("Follow-up added for Mediation " + hearing.getSummonNumber());
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
            snapshot.put("Added By", officer.getPerson().getFirstName() + " " + officer.getPerson().getLastName());

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
                minutes.getRecordedBy().getPerson().getFirstName() + " " + minutes.getRecordedBy().getPerson().getLastName()
        );

        List<FollowUpSummaryDTO> followUps = hearing.getFollowUps().stream()
                .map(f -> new FollowUpSummaryDTO(
                        f.getId(),
                        f.getRemarks(),
                        f.getRecordedBy().getPerson().getFirstName() + " " + f.getRecordedBy().getPerson().getLastName(),
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

        boolean hasCreatePerm = officer.getCustomPermissions().stream()
                .anyMatch(p -> p.getPermissionName().equalsIgnoreCase("Manage Hearings & Mediation"));

        if ( !hasCreatePerm) {
            throw new RuntimeException("Access Denied: You don't have permission to schedule hearings.");
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

        CaseTimeline timeline = new CaseTimeline();
        timeline.setBlotterCase(bc);
        timeline.setEventType(eventType);
        timeline.setTitle("Mediation " + hearing.getSummonNumber() + " Result: " + dto.outcome());



        timeline.setDescription("Summary: " + dto.hearingNotes() + additionalTimelineDesc);
        timeline.setPerformedBy(managedOfficer);
        caseTimeLineRepository.save(timeline);

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
            snapshot.put("Officer In-Charge", officer.getPerson().getFirstName() + " " + officer.getPerson().getLastName());

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


    @Transactional
    public void updateHearingStatus(Long hearingId, String newStatus, String remarks, User actor, String ipAddress) {

        Hearing hearing = hearingRepository.findById(hearingId)
                .orElseThrow(() -> new EntityNotFoundException("Mediation not found with ID: " + hearingId));


        String oldStatus = hearing.getStatus().name();

        HearingStatus statusEnum;
        try {
            statusEnum = HearingStatus.valueOf(newStatus.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid Mediation Status: " + newStatus);
        }

        if (hearing.getStatus() == HearingStatus.COMPLETED) {
            throw new IllegalStateException("Cannot update Mediation status is also COMPLETED.");
        }

        hearing.setStatus(statusEnum);

        HearingFollowUp followUp = new HearingFollowUp();
        followUp.setHearing(hearing);
        followUp.setRemarks("STATUS CHANGED TO " + statusEnum.name() + ". Reason: " + remarks);

        hearing.getFollowUps().add(followUp);
        hearingRepository.save(hearing);

        CaseTimeline timeline = new CaseTimeline();
        timeline.setBlotterCase(hearing.getBlotterCase());
        timeline.setEventType(TimelineEventType.HEARING_FOLLOWUP);
        timeline.setTitle("Mediation " + statusEnum.name());
        timeline.setDescription("Mediation Session " + hearing.getSummonNumber());
        timeline.setPerformedBy(actor);

        caseTimeLineRepository.save(timeline);

        String blotterNum = hearing.getBlotterCase().getBlotterNumber();
        auditLogService.log(
                actor,
                Departments.LUPONG_TAGAPAMAYAPA,
                "MEDIATION MINUTES",
                Severity.INFO,
                "Update Mediation Minutes",
                ipAddress,
                "Updated Mediation (Summon #" + hearing.getSummonNumber() + ") of Case #" + blotterNum + " to " + statusEnum.name() + ". Reason: " + remarks,
                oldStatus,
                statusEnum.name()
        );
    }
}

