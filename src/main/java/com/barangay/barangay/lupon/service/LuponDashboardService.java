package com.barangay.barangay.lupon.service;

import com.barangay.barangay.blotter.repository.HearingRepository;
import com.barangay.barangay.lupon.dto.dashboard.*;
import com.barangay.barangay.lupon.repository.CaseRepository;
import com.barangay.barangay.lupon.repository.PangkatHearingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class LuponDashboardService {

    private final CaseRepository caseRepository;
    private final PangkatHearingRepository hearingRepository;


    @Transactional(readOnly = true)
    public DashboardStatsDTO getMainDashboardStats() {
        String deptName = "LUPONG_TAGAPAMAYAPA";
        LocalDateTime now = LocalDateTime.now();

        LocalDateTime startOfDay = now.with(LocalTime.MIN);
        LocalDateTime endOfDay = now.with(LocalTime.MAX);

        LocalDateTime threeDaysFromNow = now.plusDays(3);

        LocalDateTime startOfMonth = now.withDayOfMonth(1).with(LocalTime.MIN);
        LocalDateTime endOfMonth = now.withDayOfMonth(now.toLocalDate().lengthOfMonth()).with(LocalTime.MAX);

        Long hearingsToday = hearingRepository.countHearingsToday(deptName, startOfDay, endOfDay);
        Long pendingCases = caseRepository.countPendingCases(deptName);
        Long nearingDeadline = caseRepository.countCasesNearingDeadline(deptName, now, threeDaysFromNow);
        Long settledThisMonth = caseRepository.countSettledThisMonth(deptName, startOfMonth, endOfMonth);

        return new DashboardStatsDTO(
                hearingsToday != null ? hearingsToday : 0L,
                pendingCases != null ? pendingCases : 0L,
                nearingDeadline != null ? nearingDeadline : 0L,
                settledThisMonth != null ? settledThisMonth : 0L
        );
    }


    @Transactional(readOnly = true)
    public List<MonthlyCaseChartDTO> getMonthlyCasesChart() {
        String deptName = "LUPONG_TAGAPAMAYAPA";
        List<MonthlyCaseChartDTO> chartData = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();

        for (int i = 5; i >= 0; i--) {
            LocalDateTime targetMonth = now.minusMonths(i);
            LocalDateTime startOfMonth = targetMonth.withDayOfMonth(1).with(LocalTime.MIN);
            LocalDateTime endOfMonth = targetMonth.withDayOfMonth(targetMonth.toLocalDate().lengthOfMonth()).with(LocalTime.MAX);

            Long count = caseRepository.countCasesByMonthRange(deptName, startOfMonth, endOfMonth);
            String monthLabel = targetMonth.getMonth().getDisplayName(TextStyle.SHORT, Locale.ENGLISH);

            chartData.add(new MonthlyCaseChartDTO(monthLabel, count != null ? count : 0L));
        }

        return chartData;
    }


    public List<CaseStatusDistributionDTO> getLuponCaseDistribution() {
        return caseRepository.getCaseStatusDistributionByDepartment("LUPONG_TAGAPAMAYAPA");
    }

    public List<RecentCaseDTO> getTop5RecentLuponCases() {
        return caseRepository.findRecentCasesByDepartment("LUPONG_TAGAPAMAYAPA", PageRequest.of(0, 5));
    }

    public List<UpcomingHearingDTO> getUpcomingLuponHearings() {
        return hearingRepository.findUpcomingHearings(
                "LUPONG_TAGAPAMAYAPA",
                LocalDateTime.now(),
                PageRequest.of(0, 5)
        );
    }
}
