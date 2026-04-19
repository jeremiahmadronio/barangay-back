package com.barangay.barangay.clearance_management.controller;

import com.barangay.barangay.clearance_management.dto.DailyCollectionResponseDTO;
import com.barangay.barangay.clearance_management.dto.RevenueResponseByCertificate;
import com.barangay.barangay.clearance_management.dto.RevenueStatsResponseDTO;
import com.barangay.barangay.clearance_management.dto.RevenueTrendDTO;
import com.barangay.barangay.clearance_management.service.RevenueService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/revenue")
public class RevenueController {

    private final RevenueService revenueService;

    @GetMapping("/stats")
    public ResponseEntity<RevenueStatsResponseDTO>  getRevenueStats(){
        return ResponseEntity.ok(revenueService.getReleasedRevenueStats());
    }


    @GetMapping("/revenue-by-template")
    public ResponseEntity<List<RevenueResponseByCertificate>> getRevenueByCertificateType(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        return ResponseEntity.ok(revenueService.getRevenueByCertificateType(startDate, endDate));
    }


    @GetMapping("/revenue-trend")
    public ResponseEntity<List<RevenueTrendDTO>> getRevenueTrend(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        return ResponseEntity.ok(revenueService.getDynamicTrend(startDate, endDate));
    }


    @GetMapping("/top5-revenue")
    public ResponseEntity<List<RevenueResponseByCertificate>> getTop5Revenue(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        return ResponseEntity.ok(revenueService.getTop5RevenueCertificates(startDate, endDate));
    }


    @GetMapping("/daily-collections")
    public ResponseEntity<List<DailyCollectionResponseDTO>> getDailyCollections(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        return ResponseEntity.ok(revenueService.getDailyCollections(startDate, endDate));
    }



}
