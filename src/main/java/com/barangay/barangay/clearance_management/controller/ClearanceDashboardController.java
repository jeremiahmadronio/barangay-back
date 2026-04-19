package com.barangay.barangay.clearance_management.controller;

import com.barangay.barangay.clearance_management.dto.DashboardStatsResponseDTO;
import com.barangay.barangay.clearance_management.dto.RecentRequestResponseDTO;
import com.barangay.barangay.clearance_management.dto.TopTemplateResponseDTO;
import com.barangay.barangay.clearance_management.dto.WeeklyIssuedTrendDTO;
import com.barangay.barangay.clearance_management.service.ClearanceDashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/clearance/dashboard")
@RequiredArgsConstructor
public class ClearanceDashboardController {

    private final ClearanceDashboardService  clearanceDashboardService;


    @GetMapping("/stats")
    public ResponseEntity<DashboardStatsResponseDTO> getStats(){
        return  ResponseEntity.ok(clearanceDashboardService.getDashboardStats());
    }


    @GetMapping("/issuance-trend")
    public ResponseEntity<List<WeeklyIssuedTrendDTO>> getWeeklyTrend() {
        return ResponseEntity.ok(clearanceDashboardService.getLastSevenDaysTrend());
    }

    @GetMapping("/top-templates")
    public ResponseEntity<List<TopTemplateResponseDTO>> getTopTemplates() {
        return ResponseEntity.ok(clearanceDashboardService.getTop5Templates());
    }


    @GetMapping("/recent-issued")
    public ResponseEntity<List<RecentRequestResponseDTO>> getRecentIssued() {
        return ResponseEntity.ok(clearanceDashboardService.getRecentIssuedCertificates());
    }




}
