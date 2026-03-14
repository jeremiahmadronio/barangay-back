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
import com.barangay.barangay.lupon.dto.ReferToLuponRequest;
import com.barangay.barangay.lupon.model.PangkatComposition;
import com.barangay.barangay.lupon.repository.PangkatAttendanceRepository;
import com.barangay.barangay.lupon.repository.PangkatCompositionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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



    @Transactional
    public void processLuponReferral(Long caseId, ReferToLuponRequest request, User actor ,String ipAddress) {
        BlotterCase blotterCase = blotterCaseRepository.findById(caseId)
                .orElseThrow(() -> new RuntimeException("Case not found"));


        Department luponDept = departmentRepository.findByNameIgnoreCase("LUPONG_TAGAPAMAYAPA")
                .orElseThrow(() -> new RuntimeException("Department 'Lupong Tagapamayapa' not found."));
        blotterCase.setDepartment(luponDept);

        blotterCase.setStatus(CaseStatus.UNDER_MEDIATION);
        blotterCase.setReferredToLuponAt(LocalDateTime.now());

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

}
