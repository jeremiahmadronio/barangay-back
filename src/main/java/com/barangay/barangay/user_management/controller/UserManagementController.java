package com.barangay.barangay.user_management.controller;

import com.barangay.barangay.audit.service.IpAddressUtils;
import com.barangay.barangay.user_management.dto.*;
import com.barangay.barangay.user_management.service.UserManagementService;
import com.barangay.barangay.security.CustomUserDetails;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

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


    @GetMapping("/stats/global")
    public ResponseEntity<UserStats> getGlobalStats() {
        UserStats globalStats = userManagementService.getGlobalDashboardStats();

        return ResponseEntity.ok(globalStats);
    }


    @PostMapping("/create-user")
    public ResponseEntity<String> createUser(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody CreateUser createUser,
            HttpServletRequest request
    ) {
        String ipAddress = IpAddressUtils.getClientIp(request);
        userManagementService.createUserAccount(createUser,userDetails.user(),ipAddress);
        return ResponseEntity.ok("Successfully created user");


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


    @GetMapping("/staff-table/global")
    public ResponseEntity<Page<UserTable>> getGlobalStaffList(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String roleName,
            @RequestParam(required = false) String departmentName,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        Page<UserTable> staffPage = userManagementService.getGlobalStaffTable(
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
            @Valid @RequestBody EditUserDTO editUserDTO,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            HttpServletRequest request
    ) {
        String ipAddress = IpAddressUtils.getClientIp(request);

        userManagementService.updateUserAccount(
                userId,
                editUserDTO,
                userDetails.user(),
                ipAddress
        );

        return ResponseEntity.ok("Staff account has been updated successfully.");
    }


    @PutMapping("/reset-password/{id}")
    public ResponseEntity<?> resetPassword(
             @PathVariable  UUID id,
          @Valid @RequestBody   ResetPasswordDTO dto,
          @AuthenticationPrincipal   CustomUserDetails actor,
             HttpServletRequest request
    ){
        String ipAddress = IpAddressUtils.getClientIp(request);
        userManagementService.resetUserPassword(id,dto,actor.user(),ipAddress);
        return ResponseEntity.ok("successfully reset password");
    }



    @GetMapping("/user-details/{userId}")
    public ResponseEntity<UserViewDTO> getUserFullDetails ( @PathVariable  UUID userId) {

        return ResponseEntity.ok(userManagementService.getUserFullDetails(userId));
    }





    @PutMapping("/{id}/status")
    public ResponseEntity<String> updateStatus(@PathVariable UUID id, @RequestBody @Valid UpdateStatusRequest req, @AuthenticationPrincipal CustomUserDetails off, HttpServletRequest sr) {
        userManagementService.updateUserStatus(id, req, off.user(), IpAddressUtils.getClientIp(sr));
        return ResponseEntity.ok("Status updated to " + req.newStatus());
    }

    @PutMapping("/{id}/lock")
    public ResponseEntity<String> lockAccount(@PathVariable UUID id, @RequestBody @Valid LockAccountRequest req, @AuthenticationPrincipal CustomUserDetails off, HttpServletRequest sr) {
        userManagementService.lockUserAccount(id, req, off.user(), IpAddressUtils.getClientIp(sr));
        return ResponseEntity.ok("Account locked until " + req.lockUntil());
    }

    @PutMapping("/{id}/unlock")
    public ResponseEntity<String> unlockAccount(@PathVariable UUID id, @AuthenticationPrincipal CustomUserDetails off, HttpServletRequest sr) {
        userManagementService.unlockUserAccount(id, off.user(), IpAddressUtils.getClientIp(sr));
        return ResponseEntity.ok("Account unlocked successfully.");
    }



}
