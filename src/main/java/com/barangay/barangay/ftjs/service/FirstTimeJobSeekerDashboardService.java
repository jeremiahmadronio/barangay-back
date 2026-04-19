package com.barangay.barangay.ftjs.service;

import com.barangay.barangay.ftjs.dto.TrendResponseDTO;
import com.barangay.barangay.ftjs.dto.dashboard.DashboardStatsResponseDTO;
import com.barangay.barangay.ftjs.dto.dashboard.FtjsRecentIssueDTO;
import com.barangay.barangay.ftjs.dto.dashboard.StatusCountDTO;
import com.barangay.barangay.ftjs.repository.FirstTimeJobSeekerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class FirstTimeJobSeekerDashboardService {

    private final FirstTimeJobSeekerRepository  ftjsRepository;


    @Transactional(readOnly = true)
    public DashboardStatsResponseDTO getDashboardStats() {
        LocalDate today = LocalDate.now();
        LocalDate startOfWeek = today.with(java.time.DayOfWeek.MONDAY);
        LocalDate lastWeekStart = startOfWeek.minusWeeks(1);

        LocalDateTime startOfWeekTime = startOfWeek.atStartOfDay();
        LocalDateTime nowTime = LocalDateTime.now();

        return new DashboardStatsResponseDTO(
                ftjsRepository.countIssuedToday(today),
                ftjsRepository.countIssuedInRange(lastWeekStart, today),
                ftjsRepository.countArchivedInRange(startOfWeekTime, nowTime),
                ftjsRepository.countNonResidentIssuedInRange(startOfWeek, today)
        );
    }




    @Transactional(readOnly = true)
    public List<TrendResponseDTO> getLastSixMonthsTrend() {
        LocalDate end = LocalDate.now();
        LocalDate start = end.minusMonths(5).withDayOfMonth(1); // 🏛️ Current month + past 5 months

        List<Object[]> results = ftjsRepository.getMonthlyTrend(start, end);

        return results.stream()
                .map(row -> new TrendResponseDTO((String) row[0], ((Number) row[1]).longValue()))
                .toList();
    }


    @Transactional(readOnly = true)
    public List<TrendResponseDTO> getLastWeekTrend() {
        LocalDate end = LocalDate.now();
        LocalDate start = end.minusDays(6);

        List<Object[]> results = ftjsRepository.getDailyTrend(start, end);

        return results.stream()
                .map(row -> new TrendResponseDTO((String) row[0], ((Number) row[1]).longValue()))
                .toList();
    }


    @Transactional(readOnly = true)
    public List<StatusCountDTO> getGlobalStatusDistribution() {
        return ftjsRepository.findAllStatusCounts();
    }


    @Transactional(readOnly = true)
    public List<FtjsRecentIssueDTO> getRecentIssues() {
        return ftjsRepository.findTop5RecentIssues();
    }
}
