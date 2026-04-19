package com.barangay.barangay.vawc.controller;

import com.barangay.barangay.vawc.dto.CategorySummaryDTO;
import com.barangay.barangay.vawc.dto.NatureStatsDTO;
import com.barangay.barangay.vawc.dto.ReportStatsDTO;
import com.barangay.barangay.vawc.dto.TrendStatsDTO;
import com.barangay.barangay.vawc.service.VawcReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/v1/vawc/report")
@RequiredArgsConstructor
public class VawcReportController {

    private final VawcReportService vawcReportService;


    @GetMapping("/stats")
    public ResponseEntity<ReportStatsDTO> getReportStats(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {

        if (startDate == null || endDate == null) {
            endDate = LocalDateTime.now();
            startDate = endDate.minusDays(30);
        }
        ReportStatsDTO stats = vawcReportService.getVawcStats(startDate, endDate);
        return ResponseEntity.ok(stats);
    }

    @GetMapping("/nature-stats")
    public ResponseEntity<List<NatureStatsDTO>> getNatureStats(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {

        return ResponseEntity.ok(vawcReportService.getNatureStats(startDate, endDate));
    }


    @GetMapping("/trend")
    public ResponseEntity<List<TrendStatsDTO>> getTrend(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {

        return ResponseEntity.ok(vawcReportService.getVawcCaseTrend(startDate, endDate));
    }


    @GetMapping("/category-summary")
    public ResponseEntity<List<CategorySummaryDTO>> getCategorySummary(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {

        return ResponseEntity.ok(vawcReportService.getCategoryReport(startDate, endDate));
    }
}
