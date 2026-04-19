package com.barangay.barangay.user_management.controller;

import com.barangay.barangay.admin_management.model.User;
import com.barangay.barangay.security.CustomUserDetails;
import com.barangay.barangay.user_management.dto.*;
import com.barangay.barangay.user_management.service.AdminDashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
@RequestMapping("/api/v1/admin")
@RestController
public class AdminDashboardController {

    private final AdminDashboardService adminDashboardService;


    @GetMapping("/stats")
    public ResponseEntity<?> adminDashboardStats(
           @AuthenticationPrincipal CustomUserDetails actor
    ){
        return ResponseEntity.ok(adminDashboardService.getAdminStats(actor.user()));
    }

    @GetMapping("/officer-by-department")
    public ResponseEntity<List<AdminDashboardOfficerByDepartmentDTO>>countEmployeeByDepartment(){
        return ResponseEntity.ok(adminDashboardService.countEmployeeByDepartment());
    }

    @GetMapping("/resident-by-status")
    public ResponseEntity<List<AdminDashboaradResidentByStatusDTO>>getResidentByStatus(){
        return ResponseEntity.ok(adminDashboardService.getResidentDashboardStats());

    }

    @GetMapping("/recent-added")
    public ResponseEntity<List<AdminDashboardRecentAddedResidentDTO>>top5ResidentRecentAdded(){
        return ResponseEntity.ok(adminDashboardService.getRecentResidents());
    }

    @GetMapping("/recent-activity")
    public ResponseEntity<List<AdminDashboardRecentActivityDTO>>top5ResidentRecentActivity(){
        return ResponseEntity.ok(adminDashboardService.getAdminSpecificActivityFeed());
    }

    @GetMapping("/department-activity")
    public ResponseEntity<Map<String, Object>> getMonthlyTrend(
            @AuthenticationPrincipal CustomUserDetails actor) {
        return ResponseEntity.ok(adminDashboardService.getMonthlyActivityOverview(actor.user()));
    }
}
