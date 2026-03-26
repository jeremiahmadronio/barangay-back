package com.barangay.barangay.blotter.controller;

import com.barangay.barangay.blotter.dto.reports_and_display.*;
import com.barangay.barangay.blotter.service.BlotterReportsService;
import com.barangay.barangay.lupon.dto.reports.ChartDataDTO;
import com.barangay.barangay.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/blotter-reports")
@RequiredArgsConstructor
public class BlotterReportsController {

    private final BlotterReportsService  blotterCaseService;


    private LocalDateTime[] parseRange(LocalDate start, LocalDate end) {
        if (start == null || end == null) {
            end = LocalDate.now();
            start = end.withDayOfMonth(1);
        }
        return new LocalDateTime[]{start.atStartOfDay(), end.atTime(23, 59, 59)};
    }

    @GetMapping("/stats")
    public ResponseEntity<ReportsStatsDTO> getStats(
            @AuthenticationPrincipal CustomUserDetails officer,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        LocalDateTime[] r = parseRange(startDate, endDate);
        return ResponseEntity.ok(blotterCaseService.getDashboardStats(officer.user(), r[0], r[1]));
    }

    @GetMapping("/cases-trend")
    public ResponseEntity<List<ChartDataDTO>> getCasesTrend(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        LocalDateTime[] r = parseRange(startDate, endDate);
        return ResponseEntity.ok(blotterCaseService.getBlotterCaseTrend(r[0], r[1]));
    }

    @GetMapping("/nature")
    public ResponseEntity<List<NatureStatDTO>> getNatureStats(
            @AuthenticationPrincipal CustomUserDetails officer,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        LocalDateTime[] r = parseRange(startDate, endDate);
        return ResponseEntity.ok(blotterCaseService.getCasesByNature(officer.user(), r[0], r[1]));
    }

    @GetMapping("/status")
    public ResponseEntity<List<StatusStatDTO>> getStatusStats(
            @AuthenticationPrincipal CustomUserDetails officer,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        LocalDateTime[] r = parseRange(startDate, endDate);
        return ResponseEntity.ok(blotterCaseService.getOverallCasesByStatus(officer.user(), r[0], r[1]));
    }

    @GetMapping("/efficiency")
    public ResponseEntity<SettlementEfficiencyDTO> getEfficiency(
            @AuthenticationPrincipal CustomUserDetails officer,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        LocalDateTime[] r = parseRange(startDate, endDate);
        return ResponseEntity.ok(blotterCaseService.getSettlementEfficiency(officer.user(), r[0], r[1]));
    }
}
