package com.barangay.barangay.ftjs.service;

import com.barangay.barangay.enumerated.FtjsStatus;
import com.barangay.barangay.ftjs.dto.*;
import com.barangay.barangay.ftjs.model.FirstTimeJobSeeker;
import com.barangay.barangay.ftjs.repository.FirstTimeJobSeekerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class FirstTimeJobSeekerReportService {

    private final FirstTimeJobSeekerRepository ftjsRepository;



    @Transactional(readOnly = true)
    public ReportStatsResponseDTO getRangeReportStats(LocalDate start, LocalDate end) {
        if (end.isBefore(start)) {
            throw new RuntimeException("End date cannot be before start date.");
        }

        List<FtjsStatus> activeStatuses = List.of(FtjsStatus.ISSUED, FtjsStatus.RE_ISSUANCE);

        long total = ftjsRepository.countByStatusInAndDateSubmittedBetween(activeStatuses, start, end);
        long resident = ftjsRepository.countByStatusInAndResidentNotNullAndDateSubmittedBetween(activeStatuses, start, end);
        long nonResident = ftjsRepository.countByStatusInAndResidentNullAndDateSubmittedBetween(activeStatuses, start, end);

        return new ReportStatsResponseDTO(total, total, resident, nonResident);
    }



    @Transactional(readOnly = true)
    public List<StatusDistributionDTO> getFtjsStatusDistribution(LocalDate start, LocalDate end) {
        if (start == null || end == null) {
            start = (start == null) ? LocalDate.now().withDayOfMonth(1) : start;
            end = (end == null) ? LocalDate.now() : end;
        }

        if (end.isBefore(start)) {
            throw new RuntimeException("End date cannot be before start date.");
        }

        return ftjsRepository.getStatusDistribution(start, end);
    }



    @Transactional(readOnly = true)
    public List<TrendResponseDTO> getIssuanceTrend(LocalDate start, LocalDate end) {
        if (start == null || end == null) {
            start = LocalDate.now().minusMonths(1);
            end = LocalDate.now();
        }

        long daysBetween = java.time.temporal.ChronoUnit.DAYS.between(start, end);

        if (daysBetween > 366) {
            throw new RuntimeException("Range exceeds 1 year limit.");
        }

        List<Object[]> results = (daysBetween <= 30)
                ? ftjsRepository.getDailyTrend(start, end)
                : ftjsRepository.getMonthlyTrend(start, end);

        return results.stream()
                .map(row -> new TrendResponseDTO((String) row[0], ((Number) row[1]).longValue()))
                .toList();
    }


    @Transactional(readOnly = true)
    public List<FtjsReportTableDTO> getReportCases(LocalDate start, LocalDate end) {
        if (start == null || end == null) {
            start = LocalDate.now().withDayOfMonth(1);
            end = LocalDate.now();
        }

        if (end.isBefore(start)) {
            throw new RuntimeException("End date cannot be before start date.");
        }

        return ftjsRepository.findReportCasesInRange(start, end);
    }
}
