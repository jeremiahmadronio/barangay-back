package com.barangay.barangay.user_management.controller;

import com.barangay.barangay.audit.service.IpAddressUtils;
import com.barangay.barangay.user_management.dto.CreateUser;
import com.barangay.barangay.user_management.dto.UserStats;
import com.barangay.barangay.user_management.service.UserManagementService;
import com.barangay.barangay.security.CustomUserDetails;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
@RestController
@RequestMapping("/api/v1/admin-dashboard")
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
        userManagementService.createAdmin(createUser,userDetails.user(),ipAddress);
        return ResponseEntity.ok("User " + createUser.firstName() + " "  + createUser.lastName() + " has been created Successfully");


    }

}
