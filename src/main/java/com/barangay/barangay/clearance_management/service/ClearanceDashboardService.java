package com.barangay.barangay.clearance_management.service;

import com.barangay.barangay.clearance_management.dto.DashboardStatsResponseDTO;
import com.barangay.barangay.clearance_management.dto.RecentRequestResponseDTO;
import com.barangay.barangay.clearance_management.dto.TopTemplateResponseDTO;
import com.barangay.barangay.clearance_management.dto.WeeklyIssuedTrendDTO;
import com.barangay.barangay.clearance_management.repository.CertificateTemplateRepository;
import com.barangay.barangay.clearance_management.repository.IssuedCertificateRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ClearanceDashboardService {


    private final IssuedCertificateRepository  dashboardRepository;
    private final CertificateTemplateRepository  certificateTemplateRepository;


    @Transactional(readOnly = true)
    public DashboardStatsResponseDTO getDashboardStats() {
        LocalDateTime start = LocalDate.now().atStartOfDay();
        LocalDateTime end = LocalDate.now().atTime(LocalTime.MAX);

        long totalIssued = dashboardRepository.countIssuedToday(start, end);
        BigDecimal revenue = dashboardRepository.sumRevenueToday(start, end);
        long totalArchive = dashboardRepository.countArchiveToday(start, end);
        long totalFree = dashboardRepository.countFreeCertsToday(start, end);

        return new DashboardStatsResponseDTO(
                totalIssued,
                revenue != null ? revenue : BigDecimal.ZERO,
                totalArchive,
                totalFree
        );
    }


    @Transactional(readOnly = true)
    public List<WeeklyIssuedTrendDTO> getLastSevenDaysTrend() {
        LocalDateTime start = LocalDate.now().minusDays(6).atStartOfDay();
        LocalDateTime end = LocalDate.now().atTime(LocalTime.MAX);

        List<Object[]> results = dashboardRepository.getWeeklyIssuedTrend(start, end);

        return results.stream()
                .map(row -> new WeeklyIssuedTrendDTO(
                        ((java.sql.Date) row[0]).toLocalDate(),
                        ((Number) row[1]).longValue()
                )).toList();
    }

    @Transactional(readOnly = true)
    public List<TopTemplateResponseDTO> getTop5Templates() {
        return certificateTemplateRepository.findTopTemplates(PageRequest.of(0, 5));
    }


    @Transactional(readOnly = true)
    public List<RecentRequestResponseDTO> getRecentIssuedCertificates() {
        return dashboardRepository.findRecentRequests(PageRequest.of(0, 5));
    }




}
