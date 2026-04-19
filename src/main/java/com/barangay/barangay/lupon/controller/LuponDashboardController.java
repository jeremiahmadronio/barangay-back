package com.barangay.barangay.lupon.controller;

import com.barangay.barangay.lupon.dto.dashboard.*;
import com.barangay.barangay.lupon.dto.reports.ChartDataDTO;
import com.barangay.barangay.lupon.service.LuponDashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/lupon-dashboard")
@RequiredArgsConstructor
public class LuponDashboardController {

    private final LuponDashboardService luponDashboardService;

    @GetMapping("/stats")
    public ResponseEntity<DashboardStatsDTO> getMainStats() {
        return ResponseEntity.ok(luponDashboardService.getMainDashboardStats());
    }

    @GetMapping("/monthly-chart")
    public ResponseEntity<List<MonthlyCaseChartDTO>> getMonthlyChart() {
        return ResponseEntity.ok(luponDashboardService.getMonthlyCasesChart());
    }

    @GetMapping("/case-distribution")
    public ResponseEntity<List<CaseStatusDistributionDTO>> getCaseDistribution() {
        return ResponseEntity.ok(luponDashboardService.getLuponCaseDistribution());
    }

    @GetMapping("/recent")
    public ResponseEntity<List<RecentCaseDTO>> getRecentCases() {
        return ResponseEntity.ok(luponDashboardService.getTop5RecentLuponCases());
    }

    @GetMapping("/upcoming-hearings")
    public ResponseEntity<List<UpcomingHearingDTO>> getUpcomingHearings() {
        return ResponseEntity.ok(luponDashboardService.getUpcomingLuponHearings());
    }



}
