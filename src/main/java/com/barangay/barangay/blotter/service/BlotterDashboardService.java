package com.barangay.barangay.blotter.service;

import com.barangay.barangay.blotter.repository.BlotterCaseRepository;
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
public class BlotterDashboardService {

    private final BlotterCaseRepository caseRepository;
    private final HearingRepository hearingRepository;

    private static final List<String> DEPARTMENTS = List.of("BLOTTER", "LUPONG_TAGAPAMAYAPA");

    private static final List<String> BLOTTER = List.of("BLOTTER");

    @Transactional(readOnly = true)
    public DashboardStatsDTO getMainDashboardStats() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startOfDay = now.with(LocalTime.MIN);
        LocalDateTime endOfDay = now.with(LocalTime.MAX);
        LocalDateTime threeDaysFromNow = now.plusDays(3);
        LocalDateTime startOfMonth = now.withDayOfMonth(1).with(LocalTime.MIN);
        LocalDateTime endOfMonth = now.withDayOfMonth(now.toLocalDate().lengthOfMonth()).with(LocalTime.MAX);

        Long hearingsToday = hearingRepository.countHearingsToday(DEPARTMENTS, startOfDay, endOfDay);
        Long pendingCases = caseRepository.countPendingCases(DEPARTMENTS);

        // Nearing Deadline: Kasama na rito ang UNDER_MEDIATION at UNDER_CONCILIATION
        Long nearingDeadline = caseRepository.countCasesNearingDeadline(DEPARTMENTS, now, threeDaysFromNow);
        Long settledThisMonth = caseRepository.countSettledThisMonth(DEPARTMENTS, startOfMonth, endOfMonth);

        return new DashboardStatsDTO(
                hearingsToday != null ? hearingsToday : 0L,
                pendingCases != null ? pendingCases : 0L,
                nearingDeadline != null ? nearingDeadline : 0L,
                settledThisMonth != null ? settledThisMonth : 0L
        );
    }

    @Transactional(readOnly = true)
    public List<MonthlyCaseChartDTO> getMonthlyCasesChart() {
        List<MonthlyCaseChartDTO> chartData = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();

        for (int i = 5; i >= 0; i--) {
            LocalDateTime targetMonth = now.minusMonths(i);
            LocalDateTime startOfMonth = targetMonth.withDayOfMonth(1).with(LocalTime.MIN);
            LocalDateTime endOfMonth = targetMonth.withDayOfMonth(targetMonth.toLocalDate().lengthOfMonth()).with(LocalTime.MAX);

            Long count = caseRepository.countCasesByMonthRange(DEPARTMENTS, startOfMonth, endOfMonth);
            String monthLabel = targetMonth.getMonth().getDisplayName(TextStyle.SHORT, Locale.ENGLISH);

            chartData.add(new MonthlyCaseChartDTO(monthLabel, count != null ? count : 0L));
        }
        return chartData;
    }

    public List<CaseStatusDistributionDTO> getGlobalCaseDistribution() {
        return caseRepository.getCaseStatusDistributionByDepartments(DEPARTMENTS);
    }

    public List<RecentCaseDTO> getTop5RecentCases() {
        return caseRepository.findRecentCasesByDepartments(DEPARTMENTS, PageRequest.of(0, 5));
    }

    public List<UpcomingHearingDTO> getUpcomingHearings() {
        return hearingRepository.findUpcomingHearings(
                BLOTTER,
                LocalDateTime.now(),
                PageRequest.of(0, 5)
        );
    }
}