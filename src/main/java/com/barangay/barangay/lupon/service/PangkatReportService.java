package com.barangay.barangay.lupon.service;

import com.barangay.barangay.blotter.model.BlotterCase;
import com.barangay.barangay.enumerated.CaseStatus;
import com.barangay.barangay.enumerated.CaseType;
import com.barangay.barangay.lupon.dto.reports.*;
import com.barangay.barangay.lupon.repository.CaseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PangkatReportService {

    private final CaseRepository caseRepository;


    public ReportsStatsDTO getLuponReports(LocalDateTime start, LocalDateTime end) {
        LocalDateTime actualStart = (start != null) ? start : LocalDateTime.of(2020, 1, 1, 0, 0);

        LocalDateTime actualEnd = (end != null) ? end : LocalDateTime.now();

        ReportsStatsDTO stats = caseRepository.getLuponStats(
                "LUPONG_TAGAPAMAYAPA",
                actualStart,
                actualEnd
        );

        return new ReportsStatsDTO(
                stats.escalate() != null ? stats.escalate() : 0L,
                stats.totalSettled() != null ? stats.totalSettled() : 0L,
                stats.totalClosed() != null ? stats.totalClosed() : 0L,
                stats.totalCFA() != null ? stats.totalCFA() : 0L
        );
    }




    public List<StatusStatDTO> getStatusAnalytics(LocalDateTime start, LocalDateTime end) {
        LocalDateTime actualStart = (start != null) ? start : LocalDateTime.now().withDayOfYear(1);
        LocalDateTime actualEnd = (end != null) ? end : LocalDateTime.now();

        return caseRepository.getStatusDistribution(actualStart, actualEnd);
    }


    public List<NatureReportDTO> getTop5Nature(LocalDateTime start, LocalDateTime end) {
        LocalDateTime actualStart = (start != null) ? start : LocalDateTime.now().withDayOfMonth(1).withHour(0).withMinute(0);
        LocalDateTime actualEnd = (end != null) ? end : LocalDateTime.now();

        return caseRepository.getTop5NatureByLupon(actualStart, actualEnd, PageRequest.of(0, 5));
    }



    public List<ChartDataDTO> getDynamicChartData(LocalDateTime start, LocalDateTime end) {

        List<Object[]> rawData = caseRepository.getRawDailyCounts(start, end);
        if (start.isAfter(end)) {
            throw new IllegalArgumentException("Invalid date range: Start date must be before the end date.");
        }

        if (start.plusYears(1).isBefore(end)) {
            throw new IllegalArgumentException("The selected date range exceeds the maximum limit of one year. Please select a shorter period for accurate visualization.");
        }

        Map<LocalDate, Long> dbMap = rawData.stream()
                .collect(Collectors.toMap(
                        obj -> (java.time.LocalDate) obj[0],
                        obj -> (Long) obj[1],
                        (existing, replacement) -> existing
                ));

        List<ChartDataDTO> finalizedData = new ArrayList<>();
        long daysBetween = ChronoUnit.DAYS.between(start, end);
        boolean isDaily = daysBetween <= 31;

        if (isDaily) {
            generateDailyData(start, end, dbMap, finalizedData);
        } else {
            generateMonthlyData(start, end, dbMap, finalizedData);
        }

        return finalizedData;
    }

    private void generateDailyData(LocalDateTime start, LocalDateTime end, Map<LocalDate, Long> dbMap, List<ChartDataDTO> list) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM dd");
        LocalDate current = start.toLocalDate();
        LocalDate last = end.toLocalDate();

        while (!current.isAfter(last)) {
            list.add(new ChartDataDTO(
                    current.format(formatter),
                    dbMap.getOrDefault(current, 0L)
            ));
            current = current.plusDays(1);
        }
    }

    private void generateMonthlyData(LocalDateTime start, LocalDateTime end, Map<LocalDate, Long> dbMap, List<ChartDataDTO> list) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM yyyy");
        YearMonth currentYM = YearMonth.from(start);
        YearMonth endYM = YearMonth.from(end);

        while (!currentYM.isAfter(endYM)) {
            YearMonth finalCurrentYM = currentYM;
            long monthlySum = dbMap.entrySet().stream()
                    .filter(entry -> YearMonth.from(entry.getKey()).equals(finalCurrentYM))
                    .mapToLong(Map.Entry::getValue)
                    .sum();

            list.add(new ChartDataDTO(currentYM.format(formatter), monthlySum));
            currentYM = currentYM.plusMonths(1);
        }
    }



    public List<LuponMonthlyReportDTO> getMonthlyReport(int month, int year) {
        YearMonth targetMonth = YearMonth.of(year, month);
        LocalDateTime start = targetMonth.atDay(1).atStartOfDay();
        LocalDateTime end = targetMonth.atEndOfMonth().atTime(23, 59, 59);

        List<BlotterCase> cases = caseRepository.findCasesByDateRange(start, end, "LUPONG_TAGAPAMAYAPA");

        return cases.stream().map(this::mapToDTO).collect(Collectors.toList());
    }

    private LuponMonthlyReportDTO mapToDTO(BlotterCase bc) {
        LuponMonthlyReportDTO dto = new LuponMonthlyReportDTO();
        dto.setDateFiled(bc.getDateFiled().toLocalDate());
        dto.setCaseNo(bc.getBlotterNumber());

        String parties = bc.getComplainant().getPerson().getLastName() + ", " + bc.getComplainant().getPerson().getFirstName() +
                " VS. " +
                bc.getRespondent().getPerson().getLastName() + ", " + bc.getRespondent().getPerson().getFirstName();
        dto.setParties(parties.toUpperCase());

        String natureName = (bc.getIncidentDetail() != null && bc.getIncidentDetail().getNatureOfComplaint() != null)
                ? bc.getIncidentDetail().getNatureOfComplaint()
                : "OTHERS";
        dto.setComplaint(natureName);

        classifyNature(natureName, dto);

        mapResolutionStatus(bc, dto);

        return dto;
    }

    private void classifyNature(String name, LuponMonthlyReportDTO dto) {
        Set<String> criminalCases = Set.of(
                "Physical Injury", "Slander / Oral Defamation", "Theft",
                "Threats", "Trespassing", "Grave Coercion",
                "Unjust Vexation", "Public Disturbance / Scandal"
        );

        Set<String> civilCases = Set.of(
                "Debt / Financial Dispute", "Boundary / Land Dispute"
        );

        if (criminalCases.contains(name)) {
            dto.setIsCriminal(1);
        } else if (civilCases.contains(name)) {
            dto.setIsCivil(1);
        } else {
            // Lahat ng hindi pumasok sa taas (Nuisance, Parking, Ordinance, etc.) ay sa "Others"
            dto.setIsOthers(1);
        }
    }

    private void mapResolutionStatus(BlotterCase bc, LuponMonthlyReportDTO dto) {
        if (bc.getStatus() == CaseStatus.SETTLED) {
            if (bc.getLuponReferral().getExtensionCount() == 0) dto.setMediation(1); // Barangay Captain level
            else dto.setConciliation(1); // Pangkat level
        }
        else if (bc.getStatus() == CaseStatus.CERTIFIED_TO_FILE_ACTION) {
            dto.setIssueCFA(1);
        }
        else if (bc.getStatus() == CaseStatus.PENDING || bc.getStatus() == CaseStatus.UNDER_CONCILIATION) {
            dto.setOngoing(1);
        }
        else if (bc.getStatus() == CaseStatus.DISMISSED) {
            dto.setDismissed(1);
        }
    }
}
