package com.barangay.barangay.blotter.controller;

import com.barangay.barangay.blotter.dto.reports.*;
import com.barangay.barangay.blotter.service.BlotterReportsService;
import com.barangay.barangay.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/blotter-reports")
@RequiredArgsConstructor
public class BlotterReportsController {

    private final BlotterReportsService  blotterCaseService;


    @GetMapping("/stats")
    public ResponseEntity<ReportsStatsDTO> getSummary(@AuthenticationPrincipal CustomUserDetails userDetails) {
        return ResponseEntity.ok(blotterCaseService.getDashboardStats(userDetails.user()));
    }


    @GetMapping("/monthly-trends")
    public ResponseEntity<List<MonthlyTrendDTO>> getTrends(@AuthenticationPrincipal CustomUserDetails userDetails) {

        return ResponseEntity.ok(blotterCaseService.getMonthlyTrends(userDetails.user()));
    }

    @GetMapping("/cases-by-nature")
    public ResponseEntity<List<NatureStatDTO>> getCasesByNature(@AuthenticationPrincipal CustomUserDetails userDetails) {
        return ResponseEntity.ok(blotterCaseService.getCasesByNature(userDetails.user()));
    }

    @GetMapping("/cases-by-status")
    public ResponseEntity<List<StatusStatDTO>> getCasesByStatus(@AuthenticationPrincipal CustomUserDetails userDetails) {
        return ResponseEntity.ok(blotterCaseService.getOverallCasesByStatus(userDetails.user()));
    }

    @GetMapping("/settlement-efficiency")
    public ResponseEntity<SettlementEfficiencyDTO> getEfficiency(@AuthenticationPrincipal CustomUserDetails userDetails) {
        return ResponseEntity.ok(blotterCaseService.getSettlementEfficiency(userDetails.user()));
    }
}
