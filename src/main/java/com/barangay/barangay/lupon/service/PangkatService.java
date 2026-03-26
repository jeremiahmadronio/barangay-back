package com.barangay.barangay.lupon.service;

import com.barangay.barangay.admin_management.model.User;
import com.barangay.barangay.audit.model.AuditLog;
import com.barangay.barangay.audit.service.AuditLogService;
import com.barangay.barangay.blotter.dto.notes.AddCaseNoteRequest;
import com.barangay.barangay.blotter.model.*;
import com.barangay.barangay.blotter.repository.BlotterCaseRepository;
import com.barangay.barangay.blotter.repository.CaseNoteRepository;
import com.barangay.barangay.blotter.repository.CasteTimeLineRepository;
import com.barangay.barangay.blotter.repository.HearingMinutesRepository;
import com.barangay.barangay.department.model.Department;
import com.barangay.barangay.department.repository.DepartmentRepository;
import com.barangay.barangay.enumerated.*;
import com.barangay.barangay.lupon.dto.*;
import com.barangay.barangay.lupon.model.PangkatAttendance;
import com.barangay.barangay.lupon.model.PangkatComposition;
import com.barangay.barangay.lupon.repository.CaseRepository;
import com.barangay.barangay.lupon.repository.PangkatAttendanceRepository;
import com.barangay.barangay.lupon.repository.PangkatCompositionRepository;
import com.barangay.barangay.lupon.repository.PangkatHearingRepository;
import com.barangay.barangay.user_management.repository.UserManagementRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PangkatService {

    private final PangkatAttendanceRepository pangkatAttendanceRepository;
    private final PangkatCompositionRepository pangkatCompositionRepository;
    private final BlotterCaseRepository blotterCaseRepository;
    private final DepartmentRepository departmentRepository;
    private final AuditLogService auditLogService;
    private final CaseRepository caseRepository;
    private final PangkatHearingRepository hearingRepository;
    private final CasteTimeLineRepository caseTimeLineRepository;
    private final HearingMinutesRepository hearingMinutesRepository;



    LocalDateTime now = LocalDateTime.now();




    @Transactional(readOnly = true)
    public LuponCaseStatsDTO getLuponDashboardStats(User officer) {
        Long deptId = officer.getAllowedDepartments().stream()
                .findFirst()
                .map(Department::getId)
                .orElseThrow(() -> new RuntimeException("Unauthorized: No department assigned to officer."));

        return new LuponCaseStatsDTO(
                caseRepository.countTotalReferred(deptId),
                caseRepository.countActiveConciliation(deptId),
                caseRepository.countSettled(deptId),
                caseRepository.countCfaIssued(deptId)
        );
    }








    @Transactional
    public void processLuponReferral(String blotterNumber, ReferToLuponRequest request, User actor, String ipAddress) {
        BlotterCase blotterCase = blotterCaseRepository.findByBlotterNumber(blotterNumber)
                .orElseThrow(() -> new RuntimeException("Case not found: " + blotterNumber));

        Department luponDept = departmentRepository.findByNameIgnoreCase("LUPONG_TAGAPAMAYAPA")
                .orElseThrow(() -> new RuntimeException("Department 'Lupong Tagapamayapa' not found."));


        List<Hearing> activeHearings = hearingRepository.findAllByBlotterCaseBlotterNumberAndStatus(
                blotterNumber, HearingStatus.SCHEDULED);

        if (!activeHearings.isEmpty()) {
            activeHearings.forEach(h -> {
                h.setStatus(HearingStatus.CANCELLED);

            });
            hearingRepository.saveAll(activeHearings);
        }

        LocalDateTime now = LocalDateTime.now();
        blotterCase.setDepartment(luponDept);
        blotterCase.setStatus(CaseStatus.UNDER_CONCILIATION);
        blotterCase.setReferredToLuponAt(now);
        blotterCase.setLuponDeadline(now.plusDays(15));
        blotterCase.setStatusRemarks("Case refer to lupon");

        if (request.members() == null || request.members().isEmpty()) {
            throw new RuntimeException("Pangkat members are required for referral.");
        }

        List<PangkatComposition> pangkat = request.members().stream().map(dto -> {
            PangkatComposition member = new PangkatComposition();
            member.setBlotterCase(blotterCase);
            member.setFirstName(dto.firstName());
            member.setLastName(dto.lastName());
            member.setPosition(dto.position());
            return member;
        }).collect(Collectors.toList());

        pangkatCompositionRepository.saveAll(pangkat);

        // 5. Audit Logging
        auditLogService.log(
                actor,
                Departments.LUPONG_TAGAPAMAYAPA,
                "LUPON_REFERRAL",
                Severity.INFO,
                "REFER_TO_LUPON",
                ipAddress,
                "Referred Case #" + blotterCase.getBlotterNumber() + " to Lupon",
                null,
                null
        );
    }


    @Transactional(readOnly = true)
    public Page<LuponSummaryDTO> getLuponSummary(
            String search,
            Long natureId,
            LocalDate startDate,
            LocalDate endDate,
            int page,
            int size) {
        Pageable pageable = PageRequest.of(page, size);
        String luponDeptName = "LUPONG_TAGAPAMAYAPA";
        LocalDateTime start = (startDate != null) ? startDate.atStartOfDay() : null;
        LocalDateTime end = (endDate != null) ? endDate.atTime(23, 59, 59) : null;

        List<CaseStatus> allowedStatuses = Arrays.asList(
                CaseStatus.UNDER_CONCILIATION,
                CaseStatus.CERTIFIED_TO_FILE_ACTION,
                CaseStatus.SETTLED,
                CaseStatus.CLOSED
        );

        String cleanSearch = (search != null && !search.isBlank()) ? search.trim() : null;

        return caseRepository.findLuponSummaryWithFilters(
                luponDeptName,
                allowedStatuses,
                cleanSearch,
                natureId,
                start,
                end,
                pageable
        );
    }


    @Transactional
    public void processExtension(Long caseId, ExtendLuponRequest request, User actor, String ipAddress) {
        BlotterCase blotterCase = blotterCaseRepository.findById(caseId)
                .orElseThrow(() -> new EntityNotFoundException("Case not found."));

        if (blotterCase.getExtensionCount() != null && blotterCase.getExtensionCount() >= 1) {
            throw new IllegalStateException("Extension Denied: This case has already reached the maximum 1-time extension limit.");
        }

        if (blotterCase.getStatus() != CaseStatus.UNDER_CONCILIATION) {
            throw new IllegalStateException("Extension Denied: Case must be UNDER_CONCILIATION to extend.");
        }

        blotterCase.setExtensionReason(request.reason());
        blotterCase.setExtensionDate(LocalDateTime.now());

        int currentCount = (blotterCase.getExtensionCount() == null) ? 0 : blotterCase.getExtensionCount();
        blotterCase.setExtensionCount(currentCount + 1);

        if (blotterCase.getLuponDeadline() != null) {
            blotterCase.setLuponDeadline(blotterCase.getLuponDeadline().plusDays(15));
        }

        auditLogService.log(actor, Departments.LUPONG_TAGAPAMAYAPA, "LUPON_EXTENSION",
                Severity.WARNING, "EXTEND_PERIOD", ipAddress,
                "Extended Case #" + blotterCase.getBlotterNumber() + ". New Deadline: " + blotterCase.getLuponDeadline(),
                null, null);
    }



    @Transactional(readOnly = true)
    public Page<HearingScheduleDTO> getLuponHearingSchedules(String search, String tab, int page, int size) {
        int pageNumber = (page > 0) ? page - 1 : 0;

        Pageable pageable = PageRequest.of(pageNumber, size, Sort.by(Sort.Direction.ASC, "scheduledStart"));

        String luponDeptName = "LUPONG_TAGAPAMAYAPA";

        List<HearingStatus> targetStatuses;

        if (tab == null || tab.trim().isEmpty() || tab.equalsIgnoreCase("ALL")) {
            targetStatuses = Arrays.asList(HearingStatus.values());
        } else {
            String normalizedTab = tab.trim().toUpperCase();
            if (normalizedTab.equals("SCHEDULED")) {
                targetStatuses = Arrays.asList(HearingStatus.SCHEDULED);
            } else if (normalizedTab.equals("COMPLETED")) {
                targetStatuses = Arrays.asList(HearingStatus.COMPLETED);
            } else if (normalizedTab.equals("POSTPONED")) {
                targetStatuses = Arrays.asList(HearingStatus.RESCHEDULED); // UI says Postponed, DB says Rescheduled
            } else if (normalizedTab.equals("CANCELLED")) {
                targetStatuses = Arrays.asList(HearingStatus.CANCELLED);
            } else {
                targetStatuses = Arrays.asList(HearingStatus.values());
            }
        }

        String cleanSearch = (search != null && !search.isBlank()) ? "%" + search.trim() + "%" : "%%";
        return hearingRepository.findLuponHearingsWithFilters(
                luponDeptName,
                targetStatuses,
                cleanSearch,
                pageable
        );
    }




    @Transactional
    public void updateHearingStatus(Long hearingId, String newStatus, String remarks, User actor, String ipAddress) {

        Hearing hearing = hearingRepository.findById(hearingId)
                .orElseThrow(() -> new EntityNotFoundException("Hearing not found with ID: " + hearingId));


        String oldStatus = hearing.getStatus().name();

        HearingStatus statusEnum;
        try {
            statusEnum = HearingStatus.valueOf(newStatus.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid Hearing Status: " + newStatus);
        }

        if (hearing.getStatus() == HearingStatus.COMPLETED) {
            throw new IllegalStateException("Cannot update hearing status is also COMPLETED.");
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
        timeline.setTitle("Conciliation " + statusEnum.name());
        timeline.setDescription("Conciliation Session #" + hearing.getSummonNumber());
        timeline.setPerformedBy(actor);

        caseTimeLineRepository.save(timeline);

        String blotterNum = hearing.getBlotterCase().getBlotterNumber();
        auditLogService.log(
                actor,
                Departments.LUPONG_TAGAPAMAYAPA,
                "LUPON_HEARINGS",
                Severity.INFO,
                "UPDATE_HEARING_STATUS",
                ipAddress,
                "Updated Conciliation (Summon #" + hearing.getSummonNumber() + ") of Case #" + blotterNum + " to " + statusEnum.name() + ". Reason: " + remarks,
                oldStatus,
                statusEnum.name()
        );
    }




    @Transactional
    public void recordHearingMinutes(Long hearingId, RecordHearingRequestDTO request, User actor, String ipAddress) {
        Hearing hearing = hearingRepository.findById(hearingId)
                .orElseThrow(() -> new RuntimeException("Hearing not found!"));
        BlotterCase bc = hearing.getBlotterCase();

        if (request.outcome() == HearingOutcome.SETTLED &&
                (request.settlementTerms() == null || request.settlementTerms().isBlank())) {
            throw new RuntimeException("Validation Error: Settlement Terms are required for SETTLED outcome!");
        }

        List<PangkatComposition> officialPangkat = pangkatCompositionRepository.findByBlotterCaseId(bc.getId());
        if (officialPangkat.isEmpty()) {
            throw new RuntimeException("System Error: No Pangkat composition found for Case ID " + bc.getId());
        }

        HearingMinutes minutes = new HearingMinutes();
        minutes.setHearing(hearing);
        minutes.setComplainantPresent(request.complainantPresent());
        minutes.setRespondentPresent(request.respondentPresent());
        minutes.setHearingNotes(request.hearingNotes());
        minutes.setOutcome(request.outcome());
        minutes.setRecordedBy(actor);
        hearingMinutesRepository.save(minutes);

        for (PangkatAttendanceDTO attDto : request.pangkatAttendance()) {
            PangkatComposition member = officialPangkat.stream()
                    .filter(p -> p.getId().equals(attDto.pangkatMemberId()))
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException("Fraud Alert: Invalid Pangkat Member for this case!"));

            PangkatAttendance attendance = new PangkatAttendance();
            attendance.setHearing(hearing);
            attendance.setPangkatMember(member);
            attendance.setIsPresent(attDto.isPresent());
            pangkatAttendanceRepository.save(attendance);
        }

        String timelineDetail = "Conciliation conducted #" + hearing.getSummonNumber() + ". Result: " + request.outcome();
        if (request.outcome() == HearingOutcome.SETTLED) {
            bc.setStatus(CaseStatus.SETTLED);
            bc.setSettlementTerms(request.settlementTerms());
            bc.setSettledAt(LocalDateTime.now());
            bc.setStatusRemarks("Settled during Conciliation #" + hearing.getSummonNumber());
            caseRepository.save(bc);
            timelineDetail = "Case SETTLED during Conciliation #" + hearing.getSummonNumber() + ". Terms: " + request.settlementTerms();
        }

        CaseTimeline timeline = new CaseTimeline();
        timeline.setBlotterCase(bc);
        timeline.setEventType(TimelineEventType.HEARING_CONDUCTED);
        timeline.setTitle("Conciliation #" + hearing.getSummonNumber() + " Recorded");
        timeline.setDescription(timelineDetail);
        timeline.setPerformedBy(actor);
        caseTimeLineRepository.save(timeline);

        auditLogService.log(
                actor,
                Departments.LUPONG_TAGAPAMAYAPA,
                "RECORD_HEARING_MINUTES",
                Severity.INFO,
                "Recorded minutes for " + bc.getBlotterNumber() + " - Conciliation #" + hearing.getSummonNumber(),
                ipAddress,
                "Hearing Result: " + request.outcome(),
                null,
                null
        );

        hearing.setStatus(HearingStatus.COMPLETED);
        hearingRepository.save(hearing);
    }



    @Transactional(readOnly = true)
    public HearingMinutesViewingRequestDTO getHearingFullDetails(Long hearingId) {
        Hearing h = hearingRepository.findById(hearingId)
                .orElseThrow(() -> new RuntimeException("Hearing not found!"));

        BlotterCase bc = h.getBlotterCase();

        List<AssignedPangkatDTO> assignedPangkat = pangkatCompositionRepository.findByBlotterCaseId(bc.getId())
                .stream()
                .map(p -> new AssignedPangkatDTO(
                        p.getId(),
                        p.getFirstName() + " " + p.getLastName(),
                        p.getPosition()
                )).toList();

        DateTimeFormatter dateLabel = DateTimeFormatter.ofPattern("MMMM dd, yyyy");
        DateTimeFormatter timeLabel = DateTimeFormatter.ofPattern("hh:mm a");

        return new HearingMinutesViewingRequestDTO(
                h.getId(),
                h.getSummonNumber(),
                h.getStatus().toString(),
                h.getScheduledStart().format(dateLabel),
                h.getScheduledStart().format(timeLabel),
                h.getScheduledEnd().format(timeLabel),
                h.getVenue(),
                bc.getBlotterNumber(),
                bc.getComplainant().getPerson().getFirstName() + " vs " + bc.getRespondent().getPerson().getFirstName(),
                assignedPangkat
        );
    }



}
