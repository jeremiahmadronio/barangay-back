package com.barangay.barangay.ftjs.controller;


import com.barangay.barangay.ftjs.dto.TrendResponseDTO;
import com.barangay.barangay.ftjs.dto.dashboard.DashboardStatsResponseDTO;
import com.barangay.barangay.ftjs.dto.dashboard.FtjsRecentIssueDTO;
import com.barangay.barangay.ftjs.dto.dashboard.StatusCountDTO;
import com.barangay.barangay.ftjs.service.FirstTimeJobSeekerDashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/ftjs-dashboard")
@RequiredArgsConstructor
public class FirstTimeJobSeekerDashboard {


    private final FirstTimeJobSeekerDashboardService ftjsService;


    @GetMapping("/stats")
    public ResponseEntity<DashboardStatsResponseDTO> getDashboardStats() {
        return ResponseEntity.ok(ftjsService.getDashboardStats());
    }


    @GetMapping("/last-6-months")
    public ResponseEntity<List<TrendResponseDTO>> getSixMonthsTrend() {
        return ResponseEntity.ok(ftjsService.getLastSixMonthsTrend());
    }

    @GetMapping("/last-week")
    public ResponseEntity<List<TrendResponseDTO>> getLastWeekTrend() {
        return ResponseEntity.ok(ftjsService.getLastWeekTrend());
    }


    @GetMapping("/distribution-status")
    public ResponseEntity<List<StatusCountDTO>> getGlobalDistribution() {
        return ResponseEntity.ok(ftjsService.getGlobalStatusDistribution());
    }


    @GetMapping("/recent-issues")
    public ResponseEntity<List<FtjsRecentIssueDTO>> getRecentIssues() {
        return ResponseEntity.ok(ftjsService.getRecentIssues());
    }
}
