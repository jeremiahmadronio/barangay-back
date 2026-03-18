package com.barangay.barangay.lupon.service;

import com.barangay.barangay.admin_management.model.User;
import com.barangay.barangay.audit.service.AuditLogService;
import com.barangay.barangay.blotter.model.BlotterCase;
import com.barangay.barangay.blotter.repository.BlotterCaseRepository;
import com.barangay.barangay.department.model.Department;
import com.barangay.barangay.department.repository.DepartmentRepository;
import com.barangay.barangay.enumerated.CaseStatus;
import com.barangay.barangay.enumerated.Departments;
import com.barangay.barangay.enumerated.Severity;
import com.barangay.barangay.lupon.dto.ExtendLuponRequest;
import com.barangay.barangay.lupon.dto.LuponSummaryDTO;
import com.barangay.barangay.lupon.dto.ReferToLuponRequest;
import com.barangay.barangay.lupon.model.PangkatComposition;
import com.barangay.barangay.lupon.repository.CaseRepository;
import com.barangay.barangay.lupon.repository.PangkatAttendanceRepository;
import com.barangay.barangay.lupon.repository.PangkatCompositionRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
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


    LocalDateTime now = LocalDateTime.now();


    @Transactional
    public void processLuponReferral(Long caseId, ReferToLuponRequest request, User actor, String ipAddress) {
        BlotterCase blotterCase = blotterCaseRepository.findById(caseId)
                .orElseThrow(() -> new RuntimeException("Case not found"));


        Department luponDept = departmentRepository.findByNameIgnoreCase("LUPONG_TAGAPAMAYAPA")
                .orElseThrow(() -> new RuntimeException("Department 'Lupong Tagapamayapa' not found."));
        blotterCase.setDepartment(luponDept);

        blotterCase.setStatus(CaseStatus.UNDER_CONCILIATION);
        blotterCase.setReferredToLuponAt(LocalDateTime.now());
        blotterCase.setLuponDeadline(now.plusDays(15));

        List<PangkatComposition> pangkat = request.members().stream().map(dto -> {
            PangkatComposition member = new PangkatComposition();
            member.setBlotterCase(blotterCase);
            member.setFirstName(dto.firstName());
            member.setLastName(dto.lastName());
            member.setPosition(dto.position());
            return member;
        }).collect(Collectors.toList());

        pangkatCompositionRepository.saveAll(pangkat);

        auditLogService.log(actor, Departments.LUPONG_TAGAPAMAYAPA, "LUPON_REFERRAL",
                Severity.INFO, "REFER_TO_LUPON", ipAddress,
                "Referred Case #" + blotterCase.getBlotterNumber() + " to Lupon",
                null, null);


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

        String cleanSearch = (search != null && !search.isBlank()) ? search.trim() : null;
        return caseRepository.findLuponSummaryWithFilters(
                luponDeptName,
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
}
