package com.barangay.barangay.blotter.service;

import com.barangay.barangay.admin_management.model.User;
import com.barangay.barangay.blotter.dto.reports_and_display.*;
import com.barangay.barangay.blotter.repository.BlotterCaseRepository;
import com.barangay.barangay.department.model.Department;
import com.barangay.barangay.enumerated.CaseStatus;
import com.barangay.barangay.enumerated.CaseType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BlotterReportsService {

    private final BlotterCaseRepository  blotterCaseRepository;


    @Transactional(readOnly = true)
    public ReportsStatsDTO getDashboardStats(User officer, LocalDateTime start, LocalDateTime end) {
        Department dept = officer.getAllowedDepartments().stream().findFirst()
                .orElseThrow(() -> new RuntimeException("No department assigned."));

        // 1. CURRENT PERIOD (Standardized to DateFiled)
        long curTotal = blotterCaseRepository.countByDepartmentAndDateFiledBetween(dept, start, end);
        long curFormal = blotterCaseRepository.countByCaseTypeAndDepartmentAndDateFiledBetween(CaseType.FORMAL_COMPLAINT, dept, start, end);
        long curRecord = blotterCaseRepository.countByCaseTypeAndDepartmentAndDateFiledBetween(CaseType.FOR_THE_RECORD, dept, start, end);
        long curLupon = blotterCaseRepository.countAllReferredToLupon(dept.getId(), start, end);

        // 2. PREVIOUS PERIOD LOGIC
        long durationDays = java.time.Duration.between(start, end).toDays();
        if (durationDays < 0) durationDays = 0;

        LocalDateTime prevStart = start.minusDays(durationDays + 1);
        LocalDateTime prevEnd = start.minusNanos(1);

        // 3. PREVIOUS DATA (Dapat DateFiled din lahat dito!)
        long prevTotal = blotterCaseRepository.countByDepartmentAndDateFiledBetween(dept, prevStart, prevEnd);
        long prevFormal = blotterCaseRepository.countByCaseTypeAndDepartmentAndDateFiledBetween(CaseType.FORMAL_COMPLAINT, dept, prevStart, prevEnd);
        long prevRecord = blotterCaseRepository.countByCaseTypeAndDepartmentAndDateFiledBetween(CaseType.FOR_THE_RECORD, dept, prevStart, prevEnd);
        long prevLupon = blotterCaseRepository.countAllReferredToLupon(dept.getId(), prevStart, prevEnd);

        return new ReportsStatsDTO(
                curTotal, calculateTrend(curTotal, prevTotal),
                curFormal, calculateTrend(curFormal, prevFormal),
                curRecord, calculateTrend(curRecord, prevRecord),
                curLupon, calculateTrend(curLupon, prevLupon)
        );
    }

    private double calculateTrend(long current, long previous) {
        if (previous == 0) {
            return current > 0 ? 100.0 : 0.0;
        }
        return ((double) (current - previous) / previous) * 100.0;
    }



    public List<com.barangay.barangay.lupon.dto.reports.ChartDataDTO> getBlotterCaseTrend(LocalDateTime start, LocalDateTime end) {

        List<Object[]> rawData = blotterCaseRepository.getRawDailyCounts(start, end);
        if (start.isAfter(end)) {
            throw new IllegalArgumentException("Invalid date range: Start date must be before the end date.");
        }

        if (start.toLocalDate().plusYears(1).isBefore(end.toLocalDate())) {
            throw new IllegalArgumentException("The selected date range exceeds the maximum limit of one year.");
        }

        Map<LocalDate, Long> dbMap = rawData.stream()
                .collect(Collectors.toMap(
                        obj -> (java.time.LocalDate) obj[0],
                        obj -> (Long) obj[1],
                        (existing, replacement) -> existing
                ));

        List<com.barangay.barangay.lupon.dto.reports.ChartDataDTO> finalizedData = new ArrayList<>();
        long daysBetween = ChronoUnit.DAYS.between(start, end);
        boolean isDaily = daysBetween <= 31;

        if (isDaily) {
            generateDailyData(start, end, dbMap, finalizedData);
        } else {
            generateMonthlyData(start, end, dbMap, finalizedData);
        }

        return finalizedData;
    }

    private void generateDailyData(LocalDateTime start, LocalDateTime end, Map<LocalDate, Long> dbMap, List<com.barangay.barangay.lupon.dto.reports.ChartDataDTO> list) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM dd");
        LocalDate current = start.toLocalDate();
        LocalDate last = end.toLocalDate();

        while (!current.isAfter(last)) {
            list.add(new com.barangay.barangay.lupon.dto.reports.ChartDataDTO(
                    current.format(formatter),
                    dbMap.getOrDefault(current, 0L)
            ));
            current = current.plusDays(1);
        }
    }

    private void generateMonthlyData(LocalDateTime start, LocalDateTime end, Map<LocalDate, Long> dbMap, List<com.barangay.barangay.lupon.dto.reports.ChartDataDTO> list) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM yyyy");
        YearMonth currentYM = YearMonth.from(start);
        YearMonth endYM = YearMonth.from(end);

        while (!currentYM.isAfter(endYM)) {
            YearMonth finalCurrentYM = currentYM;
            long monthlySum = dbMap.entrySet().stream()
                    .filter(entry -> YearMonth.from(entry.getKey()).equals(finalCurrentYM))
                    .mapToLong(Map.Entry::getValue)
                    .sum();

            list.add(new com.barangay.barangay.lupon.dto.reports.ChartDataDTO(currentYM.format(formatter), monthlySum));
            currentYM = currentYM.plusMonths(1);
        }
    }




    @Transactional(readOnly = true)
    public List<NatureStatDTO> getCasesByNature(User officer, LocalDateTime start, LocalDateTime end) {
        boolean hasAccess = officer.getAllowedDepartments().stream()
                .anyMatch(d -> d.getName().equalsIgnoreCase("BLOTTER") ||
                        d.getName().equalsIgnoreCase("LUPONG_TAGAPAMAYAPA"));

        if (!hasAccess) {
            throw new RuntimeException("Unauthorized: Access denied for reports.");
        }

        List<Object[]> results = blotterCaseRepository.countCasesByNatureFiltered(start, end);

        return results.stream()
                .map(row -> new NatureStatDTO(
                        row[0] != null ? row[0].toString() : "Unknown Nature",
                        ((Number) row[1]).longValue()
                ))
                .collect(Collectors.toList());
    }


    @Transactional(readOnly = true)
    public List<StatusStatDTO> getOverallCasesByStatus(User officer, LocalDateTime start, LocalDateTime end) {
        boolean hasAccess = officer.getAllowedDepartments().stream()
                .anyMatch(d -> d.getName().equalsIgnoreCase("BLOTTER") ||
                        d.getName().equalsIgnoreCase("LUPONG_TAGAPAMAYAPA"));

        if (!hasAccess) {
            throw new RuntimeException("Unauthorized: Access denied for Status Reports.");
        }

        List<Object[]> results = blotterCaseRepository.countCasesByStatusFiltered(start, end);

        return results.stream()
                .map(row -> new StatusStatDTO(
                        row[0] != null ? row[0].toString() : "UNKNOWN",
                        ((Number) row[1]).longValue()
                ))
                .collect(Collectors.toList());
    }


    @Transactional(readOnly = true)
    public SettlementEfficiencyDTO getSettlementEfficiency(User officer, LocalDateTime start, LocalDateTime end) {
        boolean hasAccess = officer.getAllowedDepartments().stream()
                .anyMatch(d -> d.getName().equalsIgnoreCase("BLOTTER") ||
                        d.getName().equalsIgnoreCase("LUPONG_TAGAPAMAYAPA"));

        if (!hasAccess) {
            throw new RuntimeException("Unauthorized: You do not have access to efficiency reports.");
        }

        long totalFormal = blotterCaseRepository.countTotalFormalFiltered(start, end);
        long settled = blotterCaseRepository.countSettledFormalFiltered(start, end);

        double efficiency = 0.0;
        if (totalFormal > 0) {
            efficiency = ((double) settled / totalFormal) * 100.0;
        }

        return new SettlementEfficiencyDTO(totalFormal, settled, efficiency);
    }
}
