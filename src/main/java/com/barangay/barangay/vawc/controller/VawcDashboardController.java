package com.barangay.barangay.vawc.controller;

import com.barangay.barangay.vawc.dto.DasboardRecentCaseDTO;
import com.barangay.barangay.vawc.dto.DashboardCaseDistributionDTO;
import com.barangay.barangay.vawc.dto.DashboardStatsDTO;
import com.barangay.barangay.vawc.service.VawcDashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/vawc-dashboard")
@RequiredArgsConstructor
public class VawcDashboardController {

    private final VawcDashboardService vawcDashboardService;

    @GetMapping("/stats")
    public ResponseEntity<DashboardStatsDTO> getVawcStats() {
        return ResponseEntity.ok(vawcDashboardService.getDashboardStats());
    }

    @GetMapping("/distribution")
    public ResponseEntity<List<DashboardCaseDistributionDTO>> getCaseDistribution() {
        return ResponseEntity.ok(vawcDashboardService.getCaseDistributionData());
    }

    @GetMapping("/recent")
    public ResponseEntity<List<DasboardRecentCaseDTO>> getDasboardRecentCase() {
        return ResponseEntity.ok(vawcDashboardService.getRecentEntries());
    }
}
