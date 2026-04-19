package com.barangay.barangay.ftjs.controller;

import com.barangay.barangay.ftjs.dto.FtjsReportTableDTO;
import com.barangay.barangay.ftjs.dto.ReportStatsResponseDTO;
import com.barangay.barangay.ftjs.dto.StatusDistributionDTO;
import com.barangay.barangay.ftjs.dto.TrendResponseDTO;
import com.barangay.barangay.ftjs.service.FirstTimeJobSeekerReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/ftjs-report")
@RequiredArgsConstructor
public class FirstTimeJobSeekerReportController {

    private final FirstTimeJobSeekerReportService ftjsService;

    @GetMapping("/reports/summary")
    public ResponseEntity<ReportStatsResponseDTO> getReportByRange(
            @RequestParam (required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam (required = false)@DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        return ResponseEntity.ok(ftjsService.getRangeReportStats(startDate, endDate));
    }

    @GetMapping("/distribution")
    public ResponseEntity<List<StatusDistributionDTO>> getStatusDistribution(
            @RequestParam (required = false) @DateTimeFormat( iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam (required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        return ResponseEntity.ok(ftjsService.getFtjsStatusDistribution(startDate, endDate));
    }


    @GetMapping("/trend")
    public ResponseEntity<List<TrendResponseDTO>> getTrendData(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        return ResponseEntity.ok(ftjsService.getIssuanceTrend(startDate, endDate));
    }


    @GetMapping("/cases")
    public ResponseEntity<List<FtjsReportTableDTO>> getReportCases(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        return ResponseEntity.ok(ftjsService.getReportCases(startDate, endDate));
    }
}
