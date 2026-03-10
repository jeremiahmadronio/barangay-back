package com.barangay.barangay.user_management.controller;

import com.barangay.barangay.admin_management.model.User;
import com.barangay.barangay.audit.service.IpAddressUtils;
import com.barangay.barangay.user_management.dto.*;
import com.barangay.barangay.user_management.service.UserManagementService;
import com.barangay.barangay.security.CustomUserDetails;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Controller
@RestController
@RequestMapping("/api/v1/user-management")
@RequiredArgsConstructor
public class UserManagementController {

    private final UserManagementService userManagementService;


    @GetMapping("/stats")
    public ResponseEntity<UserStats> getStats(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        return ResponseEntity.ok(userManagementService.getDashboardStats(userDetails.user()));
    }


    @PostMapping("/create-user")
    public ResponseEntity<String> createUser(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody CreateUser createUser,
            HttpServletRequest request
    ) {
        String ipAddress = IpAddressUtils.getClientIp(request);
        userManagementService.createStaff(createUser,userDetails.user(),ipAddress);
        return ResponseEntity.ok("User " + createUser.firstName() + " "  + createUser.lastName() + " has been created Successfully");


    }


    @GetMapping("/staff-table")
    public ResponseEntity<Page<UserTable>> getStaffList(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String roleName,
            @RequestParam(required = false) String departmentName,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        Page<UserTable> staffPage = userManagementService.getStaffTable(
                userDetails.user(),
                pageable,
                search,
                roleName,
                departmentName
        );

        return ResponseEntity.ok(staffPage);
    }


    @PatchMapping("/update-user/{userId}")
    public ResponseEntity<String> updateStaff(
            @PathVariable UUID userId,
            @Valid @RequestBody EditUser editUser,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            HttpServletRequest request
    ) {
        String ipAddress = IpAddressUtils.getClientIp(request);

        userManagementService.updateStaff(
                userId,
                editUser,
                userDetails.user(),
                ipAddress
        );

        return ResponseEntity.ok("Staff account has been updated successfully.");
    }

}
