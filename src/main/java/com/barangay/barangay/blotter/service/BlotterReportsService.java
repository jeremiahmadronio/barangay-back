package com.barangay.barangay.blotter.service;

import com.barangay.barangay.admin_management.model.User;
import com.barangay.barangay.blotter.dto.reports.*;
import com.barangay.barangay.blotter.repository.BlotterCaseRepository;
import com.barangay.barangay.department.model.Department;
import com.barangay.barangay.enumerated.CaseStatus;
import com.barangay.barangay.enumerated.CaseType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BlotterReportsService {

    private final BlotterCaseRepository  blotterCaseRepository;


    @Transactional(readOnly = true)
    public ReportsStatsDTO getDashboardStats(User officer) {
        Department dept = officer.getAllowedDepartments().stream().findFirst()
                .orElseThrow(() -> new RuntimeException("No department assigned."));

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startThisMonth = now.withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0);
        LocalDateTime startLastMonth = startThisMonth.minusMonths(1);


        long curTotal = blotterCaseRepository.countByDepartmentAndCreatedAtBetween(dept, startThisMonth, now);
        long prevTotal = blotterCaseRepository.countByDepartmentAndCreatedAtBetween(dept, startLastMonth, startThisMonth);

        long curFormal = blotterCaseRepository.countByCaseTypeAndDepartmentAndCreatedAtBetween(CaseType.FORMAL_COMPLAINT, dept, startThisMonth, now);
        long prevFormal = blotterCaseRepository.countByCaseTypeAndDepartmentAndCreatedAtBetween(CaseType.FORMAL_COMPLAINT, dept, startLastMonth, startThisMonth);

        long curRecord = blotterCaseRepository.countByCaseTypeAndDepartmentAndCreatedAtBetween(CaseType.FOR_THE_RECORD, dept, startThisMonth, now);
        long prevRecord = blotterCaseRepository.countByCaseTypeAndDepartmentAndCreatedAtBetween(CaseType.FOR_THE_RECORD, dept, startLastMonth, startThisMonth);

        long curLupon = blotterCaseRepository.countByStatusAndDepartmentAndCreatedAtBetween(CaseStatus.REFERRED_TO_LUPON, dept, startThisMonth, now);
        long prevLupon = blotterCaseRepository.countByStatusAndDepartmentAndCreatedAtBetween(CaseStatus.REFERRED_TO_LUPON, dept, startLastMonth, startThisMonth);

        return new ReportsStatsDTO(
                curTotal, calculateTrend(curTotal, prevTotal),
                curFormal, calculateTrend(curFormal, prevFormal),
                curRecord, calculateTrend(curRecord, prevRecord),
                curLupon, calculateTrend(curLupon, prevLupon)
        );
    }

    private double calculateTrend(long cur, long prev) {
        if (prev == 0) return cur > 0 ? 100.0 : 0.0;
        return ((double) (cur - prev) / prev) * 100.0;
    }



    @Transactional(readOnly = true)
    public List<MonthlyTrendDTO> getMonthlyTrends(User officer) {
        Long deptId = officer.getAllowedDepartments().stream()
                .findFirst()
                .map(Department::getId)
                .orElseThrow(() -> new RuntimeException("Error: No department assigned to this officer."));

        List<Object[]> results = blotterCaseRepository.findMonthlyTrendsNative(deptId);

        return results.stream()
                .map(row -> new MonthlyTrendDTO(
                        (String) row[0],
                        ((Number) row[1]).longValue()
                ))
                .collect(Collectors.toList());
    }


    @Transactional(readOnly = true)
    public List<NatureStatDTO> getCasesByNature(User officer) {
        Long deptId = officer.getAllowedDepartments().stream()
                .findFirst()
                .map(Department::getId)
                .orElseThrow(() -> new RuntimeException("No department assigned."));

        return blotterCaseRepository.countCasesByNature(deptId);
    }


    @Transactional(readOnly = true)
    public List<StatusStatDTO> getOverallCasesByStatus(User officer) {
        Long deptId = officer.getAllowedDepartments().stream()
                .findFirst()
                .map(Department::getId)
                .orElseThrow(() -> new RuntimeException("Unauthorized: No department assigned."));

        return blotterCaseRepository.countCasesByStatus(deptId);
    }


    @Transactional(readOnly = true)
    public SettlementEfficiencyDTO getSettlementEfficiency(User officer) {
        Department dept = officer.getAllowedDepartments().stream()
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Unauthorized: No department assigned."));

        long totalFormal = blotterCaseRepository.countByDepartmentAndCaseType(dept, CaseType.FORMAL_COMPLAINT);
        long settled = blotterCaseRepository.countByDepartmentAndCaseTypeAndStatus(dept, CaseType.FORMAL_COMPLAINT, CaseStatus.SETTLED);

        double efficiency = 0.0;
        if (totalFormal > 0) {
            efficiency = ((double) settled / totalFormal) * 100.0;
        }

        return new SettlementEfficiencyDTO(totalFormal, settled, efficiency);
    }
}
