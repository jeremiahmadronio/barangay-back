package com.barangay.barangay.vawc.service;

import com.barangay.barangay.vawc.dto.CategorySummaryDTO;
import com.barangay.barangay.vawc.dto.NatureStatsDTO;
import com.barangay.barangay.vawc.dto.ReportStatsDTO;
import com.barangay.barangay.vawc.dto.TrendStatsDTO;
import com.barangay.barangay.vawc.dto.projection.CategorySummaryProjection;
import com.barangay.barangay.vawc.dto.projection.NatureStatsProjection;
import com.barangay.barangay.vawc.dto.projection.ReportStatsProjection;
import com.barangay.barangay.vawc.dto.projection.TrendStatsProjection;
import com.barangay.barangay.vawc.repository.VawcCaseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
@RequiredArgsConstructor
public class VawcReportService {

    private final VawcCaseRepository caseRepository;




    @Transactional(readOnly = true)
    public ReportStatsDTO getVawcStats(LocalDateTime startDate, LocalDateTime endDate) {
        if (startDate.isAfter(endDate)) {
            throw new RuntimeException("Error: Start date cannot be after end date.");
        }

        long daysBetween = ChronoUnit.DAYS.between(startDate, endDate);
        if (daysBetween > 366) {
            throw new RuntimeException("Error: Date range exceeds the 1-year limit (Max: 366 days).");
        }

        ReportStatsProjection projection = caseRepository.getVawcStats(startDate, endDate);

        if (projection == null || projection.getTotalCases() == 0) {
            return new ReportStatsDTO(0L, 0L, 0L, 0.0);
        }

        return new ReportStatsDTO(
                projection.getTotalCases(),
                projection.getTotalExpired() != null ? projection.getTotalExpired() : 0L,
                projection.getResolvedCases() != null ? projection.getResolvedCases() : 0L,
                projection.getAvgResolutionTime() != null ? projection.getAvgResolutionTime() : 0.0
        );
    }


    @Transactional(readOnly = true)
    public List<NatureStatsDTO> getNatureStats(LocalDateTime startDate, LocalDateTime endDate) {
        if (endDate == null) {
            endDate = LocalDateTime.now();
        }
        if (startDate == null) {
            startDate = endDate.minusDays(30);
        }

        if (startDate.isAfter(endDate)) {
            throw new RuntimeException("Error: Start date cannot be after end date.");
        }

        List<NatureStatsProjection> results = caseRepository.getNatureOfComplaintStats(startDate, endDate);

        return results.stream()
                .map(p -> new NatureStatsDTO(
                        p.getNature() != null ? p.getNature() : "Unknown",
                        p.getCount() != null ? p.getCount() : 0L
                ))
                .toList();
    }




    @Transactional(readOnly = true)
    public List<TrendStatsDTO> getVawcCaseTrend(LocalDateTime start, LocalDateTime end) {
        LocalDateTime effectiveEnd = (end == null) ? LocalDateTime.now() : end;
        LocalDateTime effectiveStart = (start == null) ? effectiveEnd.minusDays(30) : start;

        if (effectiveStart.isAfter(effectiveEnd)) {
            throw new RuntimeException("Invalid date range: The start date cannot be after the end date.");
        }

        long daysBetween = java.time.temporal.ChronoUnit.DAYS.between(effectiveStart, effectiveEnd);

        if (daysBetween > 366) {
            throw new RuntimeException("Data retrieval failed: The requested duration exceeds the one-year maximum limit.");
        }

        List<TrendStatsProjection> results = (daysBetween <= 30)
                ? caseRepository.getVawcDailyTrend(effectiveStart, effectiveEnd)
                : caseRepository.getVawcMonthlyTrend(effectiveStart, effectiveEnd);

        return results.stream()
                .map(p -> new TrendStatsDTO(p.getLabel(), p.getCount()))
                .toList();
    }


    @Transactional(readOnly = true)
    public List<CategorySummaryDTO> getCategoryReport(LocalDateTime start, LocalDateTime end) {
        LocalDateTime effectiveEnd = (end == null) ? LocalDateTime.now() : end;
        LocalDateTime effectiveStart = (start == null) ? effectiveEnd.minusDays(30) : start;

        if (effectiveStart.isAfter(effectiveEnd)) {
            throw new RuntimeException("Invalid search criteria: Start date must precede the end date.");
        }

        List<CategorySummaryProjection> results = caseRepository.getDetailedCategoryReport(effectiveStart, effectiveEnd);

        return results.stream()
                .map(p -> new CategorySummaryDTO(
                        p.getCategory(),
                        p.getTotalCases(),
                        p.getActive(),
                        p.getResolved(),
                        p.getPending(),
                        p.getPercentage()
                ))
                .toList();
    }
}
