package com.barangay.barangay.lupon.controller;


import com.barangay.barangay.lupon.dto.dashboard.*;
import com.barangay.barangay.lupon.dto.reports.*;
import com.barangay.barangay.lupon.service.LuponDashboardService;
import com.barangay.barangay.lupon.service.PangkatReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.time.Year;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/lupon-reports")
@RequiredArgsConstructor
public class PangkatReportsController {

    private final PangkatReportService pangkatReportService;


    @GetMapping("/stats")
    public ResponseEntity<ReportsStatsDTO> getStats(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end) {

        return ResponseEntity.ok(pangkatReportService.getLuponReports(start, end));
    }


    @GetMapping("/status")
    public ResponseEntity<List<StatusStatDTO>> getStatusStats(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {

        return ResponseEntity.ok(pangkatReportService.getStatusAnalytics(startDate, endDate));
    }


    @GetMapping("/top-nature")
    public ResponseEntity<List<NatureReportDTO>> getTopNature(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {

        return ResponseEntity.ok(pangkatReportService.getTop5Nature(startDate, endDate));
    }


    @GetMapping("/cases-trend")
    public ResponseEntity<?> getCasesTrend(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end) {
        try {
            List<ChartDataDTO> data = pangkatReportService.getDynamicChartData(start, end);
            return ResponseEntity.ok(data);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        }
    }


    @GetMapping("/monthly-dilg")
    public ResponseEntity<?> getMonthlyDilgReport(
            @RequestParam int month,
            @RequestParam int year) {

        if (month < 1 || month > 12) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Invalid month! Please provide a value between 1 and 12."));
        }

        if (year < 2000 || year > Year.now().getValue() + 1) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Invalid year! Bakit ka nag-ge-generate ng report sa maling panahon?"));
        }

        List<LuponMonthlyReportDTO> reportData = pangkatReportService.getMonthlyReport(month, year);

        return ResponseEntity.ok(reportData);
    }
}
