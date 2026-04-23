package com.barangay.barangay.admin_management.controller;

import com.barangay.barangay.audit.service.IpAddressUtils;
import com.barangay.barangay.enumerated.Status;
import com.barangay.barangay.security.CustomUserDetails;
import com.barangay.barangay.admin_management.dto.*;
import com.barangay.barangay.admin_management.model.User;
import com.barangay.barangay.admin_management.repository.Root_AdminRepository;
import com.barangay.barangay.admin_management.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Controller
@RestController
@RequestMapping("api/v1/users")
@RequiredArgsConstructor
    public class UserController {

        private final UserService userService;
        private final Root_AdminRepository userRepository;

        @PostMapping("/create-admin")
        public ResponseEntity<String> createAdmin(
                @Valid
                @RequestBody CreateAdmin createAdmin,
                HttpServletRequest request,
                @AuthenticationPrincipal CustomUserDetails userDetails
        ) {

             String ipAddress = IpAddressUtils.getClientIp(request);
            userService.createAdminAccount(createAdmin, userDetails.user(), ipAddress);

            return ResponseEntity.ok("Admin created successfully!");
        }



       // admin stats
        @GetMapping("/stats")
        public ResponseEntity<AdminStats> displayAdminStats(
        ){
            return ResponseEntity.ok(userService.displayAdminStats());
        }


    @GetMapping("/admin-table")
    public ResponseEntity<Page<AdminTable>> displayAdminTable(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Status status
    ) {
        return ResponseEntity.ok(userService.displayAllAdminTables(search, status, page, size));
    }



    @PatchMapping("/archive-admin")
    public ResponseEntity<String> archiveAdmin(
            @RequestParam UUID userId,
            @Valid @RequestBody ArchiveAdmin request,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            HttpServletRequest ip
    ) {
        String ipAddress = IpAddressUtils.getClientIp(ip);
        userService.archiveAdminAccount(userId, request, userDetails.user(), ipAddress);
        return ResponseEntity.ok("Admin account archived successfully.");
    }

    @PatchMapping("/unarchive-admin")
    public ResponseEntity<String> unarchiveAdmin(
            @RequestParam UUID userId,
            @Valid @RequestBody ArchiveAdmin request,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            HttpServletRequest httpRequest
    ) {
        String ipAddress = IpAddressUtils.getClientIp(httpRequest);
        userService.unarchiveAdminAccount(userId,request, userDetails.user(), ipAddress);
        return ResponseEntity.ok("Admin account restored successfully.");
    }




        // Update Admin
        @PutMapping("/update-admin")
        public ResponseEntity<String> updateAdminProfile(
                @RequestParam UUID userId,
                @Valid @RequestBody UpdateAdmin updateDto,
                @RequestParam UUID actorId,
                HttpServletRequest request
        ) {
            User user = userRepository.findById(actorId).
                    orElseThrow(() -> new RuntimeException("user not found."));

            if(!user.getRole().getRoleName().equals("ROOT_ADMIN")){
                throw new RuntimeException("Only root admin can access.");
            }

            String ipAddress = IpAddressUtils.getClientIp(request);

            userService.updateAdminAccount(userId, updateDto, user, ipAddress);
            return ResponseEntity.ok("Admin profile updated successfully and logged in audit trails.");
        }

    @GetMapping("/check-email")
    public ResponseEntity<Boolean> checkEmailAvailability(@RequestParam String email) {
        return ResponseEntity.ok(userService.isEmailTaken(email));
    }

    @GetMapping("/check-backup")
    public ResponseEntity<Boolean> checkBackupEmailAvailability(@RequestParam String email) {
        return ResponseEntity.ok(userService.isBackupEmailTaken(email));
    }

    @GetMapping("/check-username")
    public ResponseEntity<Boolean> checkUsernameAvailability(@RequestParam String username) {
        return ResponseEntity.ok(userService.isUsernameTaken(username));
    }


        @PatchMapping("/{userId}/lock")
        public ResponseEntity<String> toggleUserLock(
                @PathVariable UUID userId,
                @RequestParam boolean lock,
                @Valid @RequestBody UserActionRequest actionRequest,
                @AuthenticationPrincipal CustomUserDetails userDetails,
                HttpServletRequest request
        ) {


            String ipAddress = IpAddressUtils.getClientIp(request);
            User actor = userDetails.user();

            userService.toggleUserLock(userId, lock, actor, ipAddress, actionRequest);

            String statusMessage = lock ? "locked" : "unlocked";
            return ResponseEntity.ok("User account has been successfully " + statusMessage + ".");
        }


        @PatchMapping("/update-status")
        public ResponseEntity<String> updateUserStatus(
                @RequestParam UUID userId,
                @RequestParam Status status,
                @Valid @RequestBody UserActionRequest actionRequest,
                @AuthenticationPrincipal CustomUserDetails userDetails,
                HttpServletRequest request
        ) {
            String ipAddress = IpAddressUtils.getClientIp(request);

            userService.updateUserStatus(userId, status, userDetails.user(), ipAddress, actionRequest.reason());

            String actionName = (status == Status.ACTIVE) ? "restored" : "deactivated";
            return ResponseEntity.ok("User account has been successfully " + actionName + ".");
        }


        @GetMapping("/settings-preview")
        public ResponseEntity<UserSettingsPreview> getSettings(
                @AuthenticationPrincipal CustomUserDetails userDetails) {
            return ResponseEntity.ok(userService.getMySettings(userDetails.user()));
        }

        @PutMapping("/update-settings")
        public ResponseEntity<String> updateSettings(

                @AuthenticationPrincipal CustomUserDetails userDetails,
                @Valid @RequestBody UserSettings dto,
                HttpServletRequest request) {

            String ipAddress = IpAddressUtils.getClientIp(request);
            userService.updateMySettings(userDetails.user(), dto, ipAddress);

            return ResponseEntity.ok("Profile updated successfully!");
        }

    }
