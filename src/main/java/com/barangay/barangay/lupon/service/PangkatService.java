package com.barangay.barangay.lupon.service;

import com.barangay.barangay.admin_management.model.User;
import com.barangay.barangay.audit.service.AuditLogService;
import com.barangay.barangay.blotter.dto.complaint.ArchiveCaseDTO;
import com.barangay.barangay.blotter.dto.reports_and_display.ArchiveStatsDTO;
import com.barangay.barangay.blotter.dto.reports_and_display.ArchiveTableDTO;
import com.barangay.barangay.blotter.model.*;
import com.barangay.barangay.blotter.repository.BlotterCaseRepository;
import com.barangay.barangay.blotter.repository.CasteTimeLineRepository;
import com.barangay.barangay.blotter.repository.HearingMinutesRepository;
import com.barangay.barangay.blotter.repository.LuponReferralRepository;
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
import com.barangay.barangay.employee.model.Employee;
import com.barangay.barangay.employee.repository.EmployeeRepository;
import com.barangay.barangay.person.repository.PersonRepository;
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
import java.util.*;
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
    private final PersonRepository personRepository;
    private final EmployeeRepository employeeRepository;
    private final LuponReferralRepository luponReferralRepository;



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
        // 1. Define 'now' para hindi mag-error (Dito ka nadale kanina)
        LocalDateTime now = LocalDateTime.now();

        // 2. Fetch the Case
        BlotterCase blotterCase = blotterCaseRepository.findByBlotterNumber(blotterNumber)
                .orElseThrow(() -> new EntityNotFoundException("Case not found: " + blotterNumber));

        // 3. Fetch Lupon Department
        Department luponDept = departmentRepository.findByNameIgnoreCase("LUPONG_TAGAPAMAYAPA")
                .orElseThrow(() -> new RuntimeException("Department 'Lupong Tagapamayapa' not found."));

        // 4. Cancel Active Hearings (Cleaning up previous department's schedule)
        List<Hearing> activeHearings = hearingRepository.findAllByBlotterCaseBlotterNumberAndStatus(
                blotterNumber, HearingStatus.SCHEDULED);

        if (!activeHearings.isEmpty()) {
            activeHearings.forEach(h -> {
                h.setStatus(HearingStatus.CANCELLED);
                h.setNotes("System: Automatically cancelled due to Lupon escalation.");
            });
            hearingRepository.saveAll(activeHearings);
        }

        LuponReferral luponReferral = new LuponReferral();
        luponReferral.setBlotterCase(blotterCase);
        luponReferral.setReferredAt(now);
        luponReferral.setDeadline(now.plusDays(15));
        luponReferral = luponReferralRepository.save(luponReferral);


        blotterCase.setDepartment(luponDept);
        blotterCase.setStatus(CaseStatus.UNDER_CONCILIATION);
        blotterCase.setStatusRemarks("Case referred to Lupon for formal conciliation.");
        blotterCase.setLuponReferral(luponReferral);

        blotterCaseRepository.save(blotterCase);

        // 6. Save Pangkat Composition (Linking to Employees)
        if (request.members() == null || request.members().size() != 3) {
            throw new IllegalArgumentException("Exactly 3 Pangkat members (Chairman, Secretary, Member) are required.");
        }

        List<PangkatComposition> pangkat = request.members().stream().map(dto -> {
            PangkatComposition member = new PangkatComposition();
            member.setBlotterCase(blotterCase);

            Employee emp = employeeRepository.findById(dto.employeeId())
                    .orElseThrow(() -> new EntityNotFoundException("Employee not found with ID: " + dto.employeeId()));

            member.setEmployee(emp); // Naka-link na sa Master Pool
            member.setPosition(dto.position()); // "Chairman", "Secretary", etc.

            return member;
        }).collect(Collectors.toList());

        pangkatCompositionRepository.saveAll(pangkat);

        // 7. Audit Logging
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

        LuponReferral referral = blotterCase.getLuponReferral();

        if (referral == null) {
            throw new IllegalStateException("Extension Denied: This case has not been referred to Lupon yet.");
        }

        if (blotterCase.getStatus() != CaseStatus.UNDER_CONCILIATION) {
            throw new IllegalStateException("Extension Denied: Case status is " + blotterCase.getStatus() + ". Must be UNDER_CONCILIATION.");
        }

        if (referral.getExtensionCount() != null && referral.getExtensionCount() >= 1) {
            throw new IllegalStateException("Extension Denied: Maximum 1-time extension reached (KP Law Limit).");
        }

        referral.setExtensionReason(request.reason());
        referral.setExtensionAt(LocalDateTime.now());

        int currentCount = (referral.getExtensionCount() == null) ? 0 : referral.getExtensionCount();
        referral.setExtensionCount(currentCount + 1);

        if (referral.getDeadline() != null) {
            referral.setDeadline(referral.getDeadline().plusDays(15));
        }


        luponReferralRepository.save(referral);

        CaseTimeline timeline = new CaseTimeline();
        timeline.setBlotterCase(blotterCase);
        timeline.setPerformedBy(actor);
        timeline.setEventType(TimelineEventType.CONCILIATION_EXTENDED);
        timeline.setTitle("Conciliation Period Extended");
        timeline.setDescription("The conciliation period was extended by 15 days. New deadline: "
                + referral.getDeadline().toLocalDate() + ". Reason: " + request.reason());

        caseTimeLineRepository.save(timeline);

        // 8. Audit Logging
        auditLogService.log(
                actor,
                Departments.LUPONG_TAGAPAMAYAPA,
                "LUPON_EXTENSION",
                Severity.WARNING,
                "EXTEND_PERIOD",
                ipAddress,
                "Extended Case #" + blotterCase.getBlotterNumber() + ". New Deadline: " + referral.getDeadline(),
                null,
                null
        );
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
                targetStatuses = List.of(HearingStatus.SCHEDULED);
            } else if (normalizedTab.equals("COMPLETED")) {
                targetStatuses = List.of(HearingStatus.COMPLETED);
            } else if (normalizedTab.equals("POSTPONED")) {
                targetStatuses = List.of(HearingStatus.RESCHEDULED);
            } else if (normalizedTab.equals("CANCELLED")) {
                targetStatuses = List.of(HearingStatus.CANCELLED);
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
                        p.getEmployee().getPerson().getFirstName() + " " + p.getEmployee().getPerson().getLastName(),
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




    @Transactional(readOnly = true)
    public HearingMinutesViewingDTO getHearingMinutesInfo(Long hearingId) {
        Hearing h = hearingRepository.findById(hearingId)
                .orElseThrow(() -> new EntityNotFoundException("Hearing not found with ID: " + hearingId));

        BlotterCase bc = h.getBlotterCase();
        LuponReferral referral = bc.getLuponReferral();

        HearingMinutes minutes = hearingMinutesRepository.findByHearingId(hearingId)
                .orElseThrow(() -> new EntityNotFoundException("Minutes not yet recorded."));

        boolean isLuponStageHearing = false;

        if (referral != null && referral.getReferredAt() != null) {
            if (!h.getScheduledStart().isBefore(referral.getReferredAt())) {
                isLuponStageHearing = true;
            }
        }

        Boolean chairmanPresent = null;
        Boolean secretaryPresent = null;
        Boolean memberPresent = null;

        if (isLuponStageHearing) {
            List<PangkatAttendance> attendanceList = pangkatAttendanceRepository.findByHearingId(hearingId);

            chairmanPresent = attendanceList.stream()
                    .anyMatch(a -> "Chairman".equalsIgnoreCase(a.getPangkatMember().getPosition()) && a.getIsPresent());
            secretaryPresent = attendanceList.stream()
                    .anyMatch(a -> "Secretary".equalsIgnoreCase(a.getPangkatMember().getPosition()) && a.getIsPresent());
            memberPresent = attendanceList.stream()
                    .anyMatch(a -> "Member".equalsIgnoreCase(a.getPangkatMember().getPosition()) && a.getIsPresent());
        }

        List<FollowUpSummaryDTO> followUpNotes = h.getFollowUps().stream()
                .map(f -> new FollowUpSummaryDTO(
                        f.getId(), f.getRemarks(),
                        f.getRecordedBy() != null ? f.getRecordedBy().getPerson().getFirstName() + " " + f.getRecordedBy().getPerson().getLastName() : "System",
                        f.getCreatedAt()
                )).collect(Collectors.toList());

        // 6. Return DTO
        return new HearingMinutesViewingDTO(
                h.getId(),
                h.getSummonNumber().longValue(),
                h.getStatus(),
                h.getScheduledStart(),
                h.getVenue(),
                minutes.getComplainantPresent(),
                minutes.getRespondentPresent(),
                chairmanPresent,
                secretaryPresent,
                memberPresent,
                bc.getNarrativeStatement() != null ? bc.getNarrativeStatement().getStatement() : "No narrative",
                minutes.getOutcome() != null ? minutes.getOutcome().name() : "PENDING",
                minutes.getRecordedBy() != null ? minutes.getRecordedBy().getPerson().getFirstName() + " " + minutes.getRecordedBy().getPerson().getLastName() : "Unknown",
                followUpNotes,
                isLuponStageHearing
        );
    }





    public void archiveCaseLupon(Long id, ArchiveCaseDTO dto, User actor , String ipAddress) {
        BlotterCase bc = blotterCaseRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Case not found"));

        bc.setIsArchived(true);


        bc.setArchivedRemarks(dto.reason());

        blotterCaseRepository.save(bc);
        auditLogService.log(
                actor,
                Departments.LUPONG_TAGAPAMAYAPA,
                "Archive Case",
                Severity.INFO,
                "Update Case Status — Archived",
                ipAddress,
                dto.reason(),
                "Active",
                "Inactive"



        );
    }



    @Transactional
    public void restoreCaseLupon(Long id,ArchiveCaseDTO dto,User actor , String ipAddress) {
        BlotterCase bc = blotterCaseRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Case not found"));

        bc.setIsArchived(false);

        String updatedRemarks = (bc.getStatusRemarks() != null ? bc.getStatusRemarks() : "")
                + dto.reason();
        bc.setArchivedRemarks(updatedRemarks);

        blotterCaseRepository.save(bc);

        auditLogService.log(
                actor,
                Departments.LUPONG_TAGAPAMAYAPA,
                "Restore Case",
                Severity.INFO,
                "Update Case Status — Active",
                ipAddress,
                dto.reason(),
                "Inactive",
                "Active"



        );
    }



    @Transactional(readOnly = true)
    public Page<ArchiveTableDTO> getArchivedCases(
            String search,
            CaseType caseType,
            LocalDateTime dateFrom,
            LocalDateTime dateTo,
            Pageable pageable
    ) {
        String trimmedSearch = (search != null && !search.isBlank()) ? search.trim() : null;
        return blotterCaseRepository.findArchivedCasesLupon(trimmedSearch, caseType, dateFrom, dateTo, pageable);
    }


    @Transactional(readOnly = true)
    public ArchiveLuponStats getLuponArchiveStats() {
        LocalDateTime startOfMonth = LocalDate.now()
                .withDayOfMonth(1)
                .atStartOfDay();

        ArchiveLuponStats stats = blotterCaseRepository.getLuponArchiveStatistics(startOfMonth);

        return new ArchiveLuponStats(
                stats.totalArchived() != null ? stats.totalArchived() : 0L,
                stats.archivedThisMonth() != null ? stats.archivedThisMonth() : 0L,
                stats.totalArchiveSettled() != null ? stats.totalArchiveSettled() : 0L,
                stats.totalArchiveCFA() != null ? stats.totalArchiveCFA() : 0L
        );
    }

}
