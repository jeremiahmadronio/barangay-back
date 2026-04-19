package com.barangay.barangay.vawc.service;

import com.barangay.barangay.blotter.model.BlotterCase;
import com.barangay.barangay.blotter.repository.BlotterCaseRepository;
import com.barangay.barangay.enumerated.BpoStatus;
import com.barangay.barangay.enumerated.CaseStatus;
import com.barangay.barangay.vawc.dto.DasboardRecentCaseDTO;
import com.barangay.barangay.vawc.dto.DashboardCaseDistributionDTO;
import com.barangay.barangay.vawc.dto.DashboardStatsDTO;
import com.barangay.barangay.vawc.repository.BarangayProtectionOrderRepository;
import com.barangay.barangay.vawc.repository.VawcCaseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class VawcDashboardService {


    private final VawcCaseRepository caseRepository;
    private final BarangayProtectionOrderRepository baranggayProtectionOrderRepository;
    private static final String DEPT_VAWC = "VAWC";


    @Transactional(readOnly = true)
    public DashboardStatsDTO getDashboardStats() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startThisMonth = now.withDayOfMonth(1).with(LocalTime.MIN);
        LocalDateTime startLastMonth = startThisMonth.minusMonths(1);

        long currentCases = caseRepository.countByDepartmentNameAndDateFiledAfter(DEPT_VAWC, startThisMonth);
        long prevCases = caseRepository.countByDepartmentNameAndDateFiledBetween(DEPT_VAWC, startLastMonth, startThisMonth);

        long currentSettled = caseRepository.countByDepartmentNameAndStatusAndSettledAtAfter(DEPT_VAWC, CaseStatus.SETTLED, startThisMonth);
        long prevSettled = caseRepository.countByDepartmentNameAndStatusAndSettledAtBetween(DEPT_VAWC, CaseStatus.SETTLED, startLastMonth, startThisMonth);

        long currentBpos = baranggayProtectionOrderRepository.countByCreatedAtAfter(startThisMonth);
        long prevBpos = baranggayProtectionOrderRepository.countByCreatedAtBetween(startLastMonth, startThisMonth);

        long activeBposCount = baranggayProtectionOrderRepository.countByStatus(BpoStatus.ISSUED);

        return new DashboardStatsDTO(
                currentCases, calculateTrend(currentCases, prevCases),
                activeBposCount,
                currentSettled, calculateTrend(currentSettled, prevSettled),
                currentBpos, calculateTrend(currentBpos, prevBpos)
        );
    }

    private double calculateTrend(long current, long previous) {
        if (previous == 0) return current > 0 ? 100.0 : 0.0;
        return ((double)(current - previous) / previous) * 100;
    }


    public List<DashboardCaseDistributionDTO> getCaseDistributionData() {
        List<String> rawComplaints = caseRepository.findAllNatureOfComplaintsByDept("VAWC");

        Map<String, Long> distributionMap = rawComplaints.stream()
                .filter(Objects::nonNull)
                .flatMap(s -> Arrays.stream(s.split(",")))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.groupingBy(
                        String::toUpperCase,
                        Collectors.counting()
                ));

        return distributionMap.entrySet().stream()
                .map(entry -> new DashboardCaseDistributionDTO(entry.getKey(), entry.getValue()))
                .collect(Collectors.toList());
    }


    @Transactional(readOnly = true)
    public List<DasboardRecentCaseDTO> getRecentEntries() {
        List<BlotterCase> cases = caseRepository.findTop6ByDepartmentNameOrderByDateFiledDesc("VAWC");

        return cases.stream().map(c -> {
            String prefix = "";
            if (c.getComplainant() != null && c.getComplainant().getPerson().getGender() != null) {
                prefix = c.getComplainant().getPerson().getGender().equalsIgnoreCase("MALE") ? "Mr. " : "Ms. ";
            }

            String fullName = prefix + c.getComplainant().getPerson().getFirstName() + " " + c.getComplainant().getPerson().getLastName();

            return new DasboardRecentCaseDTO(
                    c.getBlotterNumber(),
                    fullName,
                    c.getIncidentDetail() != null ? c.getIncidentDetail().getNatureOfComplaint() : "N/A",
                    c.getStatus().toString()
            );
        }).collect(Collectors.toList());
    }


}