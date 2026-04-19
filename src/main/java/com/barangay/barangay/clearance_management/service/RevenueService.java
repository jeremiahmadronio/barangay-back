package com.barangay.barangay.clearance_management.service;

import com.barangay.barangay.clearance_management.dto.DailyCollectionResponseDTO;
import com.barangay.barangay.clearance_management.dto.RevenueResponseByCertificate;
import com.barangay.barangay.clearance_management.dto.RevenueStatsResponseDTO;
import com.barangay.barangay.clearance_management.dto.RevenueTrendDTO;
import com.barangay.barangay.clearance_management.repository.CertificateTemplateRepository;
import com.barangay.barangay.clearance_management.repository.RevenueRecordRepository;
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
public class RevenueService {

    private final RevenueRecordRepository revenueRepository;
    private final CertificateTemplateRepository templateRepository;


    @Transactional(readOnly = true)
    public RevenueStatsResponseDTO getReleasedRevenueStats() {
        LocalDateTime now = LocalDateTime.now();

        LocalDateTime startOfWeek = now.with(java.time.DayOfWeek.MONDAY).withHour(0).withMinute(0);
        LocalDateTime startOfMonth = now.withDayOfMonth(1).withHour(0).withMinute(0);
        LocalDateTime startOfYear = now.withDayOfYear(1).withHour(0).withMinute(0);

        BigDecimal total = revenueRepository.sumTotalReleasedRevenue();
        BigDecimal weekly = revenueRepository.sumReleasedRevenueSince(startOfWeek);
        BigDecimal monthly = revenueRepository.sumReleasedRevenueSince(startOfMonth);
        BigDecimal yearly = revenueRepository.sumReleasedRevenueSince(startOfYear);

        return new RevenueStatsResponseDTO(
                total != null ? total : BigDecimal.ZERO,
                weekly != null ? weekly : BigDecimal.ZERO,
                monthly != null ? monthly : BigDecimal.ZERO,
                yearly != null ? yearly : BigDecimal.ZERO
        );
    }


    @Transactional(readOnly = true)
    public List<RevenueResponseByCertificate> getRevenueByCertificateType(LocalDate start, LocalDate end) {

        LocalDateTime startDateTime = start.atStartOfDay();
        LocalDateTime endDateTime = end.atTime(LocalTime.MAX);

        return templateRepository.getRevenueByCertificateType(startDateTime, endDateTime);
    }


    @Transactional(readOnly = true)
    public List<RevenueTrendDTO> getDynamicTrend(LocalDate start, LocalDate end) {
        LocalDateTime startDT = start.atStartOfDay();
        LocalDateTime endDT = end.atTime(LocalTime.MAX);

        long daysBetween = java.time.temporal.ChronoUnit.DAYS.between(start, end);

        List<Object[]> results;
        if (daysBetween <= 31) {
            results = revenueRepository.getDailyRevenueTrend(startDT, endDT);
        } else {
            results = revenueRepository.getMonthlyRevenueTrend(startDT, endDT);
        }

        return results.stream()
                .map(row -> new RevenueTrendDTO(
                        row[0].toString(),
                        new BigDecimal(row[1].toString())
                )).toList();
    }


    @Transactional(readOnly = true)
    public List<RevenueResponseByCertificate> getTop5RevenueCertificates(LocalDate start, LocalDate end) {
        LocalDateTime startDateTime = start.atStartOfDay();
        LocalDateTime endDateTime = end.atTime(LocalTime.MAX);

        return templateRepository.findTopRevenueCertificates(
                startDateTime,
                endDateTime,
                PageRequest.of(0, 5)
        );
    }


    @Transactional(readOnly = true)
    public List<DailyCollectionResponseDTO> getDailyCollections(LocalDate start, LocalDate end) {
        LocalDateTime startDT = start.atStartOfDay();
        LocalDateTime endDT = end.atTime(LocalTime.MAX);

        List<Object[]> results = revenueRepository.getDailyCollectionsReleasedAndActiveNative(startDT, endDT);

        return results.stream()
                .map(row -> new DailyCollectionResponseDTO(
                        ((java.sql.Date) row[0]).toLocalDate(),
                        ((Number) row[1]).longValue(),
                        row[2] != null ? new BigDecimal(row[2].toString()) : BigDecimal.ZERO,
                        row[3] != null ? row[3].toString() : "N/A"
                )).toList();
    }

}
