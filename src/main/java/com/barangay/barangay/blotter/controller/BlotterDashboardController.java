package com.barangay.barangay.blotter.controller;


import com.barangay.barangay.blotter.service.BlotterDashboardService;
import com.barangay.barangay.lupon.dto.dashboard.*;
import com.barangay.barangay.lupon.service.LuponDashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/blotter-dashboard")
@RequiredArgsConstructor
public class BlotterDashboardController {

    private final BlotterDashboardService dashboardService;

    @GetMapping("/stats")
    public ResponseEntity<DashboardStatsDTO> getMainStats() {
        return ResponseEntity.ok(dashboardService.getMainDashboardStats());
    }

    @GetMapping("/monthly-chart")
    public ResponseEntity<List<MonthlyCaseChartDTO>> getMonthlyChart() {
        return ResponseEntity.ok(dashboardService.getMonthlyCasesChart());
    }

    @GetMapping("/case-distribution")
    public ResponseEntity<List<CaseStatusDistributionDTO>> getCaseDistribution() {
        return ResponseEntity.ok(dashboardService.getGlobalCaseDistribution());
    }

    @GetMapping("/recent")
    public ResponseEntity<List<RecentCaseDTO>> getRecentCases() {
        return ResponseEntity.ok(dashboardService.getTop5RecentCases());
    }

    @GetMapping("/upcoming-hearings")
    public ResponseEntity<List<UpcomingHearingDTO>> getUpcomingHearings() {
        return ResponseEntity.ok(dashboardService.getUpcomingHearings());
    }
}
