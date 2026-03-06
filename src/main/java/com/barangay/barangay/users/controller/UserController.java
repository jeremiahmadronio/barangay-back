package com.barangay.barangay.users.controller;

import com.barangay.barangay.audit.service.IpAddressUtils;
import com.barangay.barangay.enumerated.Status;
import com.barangay.barangay.security.CustomUserDetails;
import com.barangay.barangay.users.dto.*;
import com.barangay.barangay.users.model.User;
import com.barangay.barangay.users.repository.UserRepository;
import com.barangay.barangay.users.service.UserService;
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
    private final UserRepository userRepository;

    //Creating admin account endpoint
    @PostMapping("/create-admin")
    public ResponseEntity<String> createAdmin(
            @Valid
            @RequestBody CreateAdmin createAdmin,
            @RequestParam UUID actorId,
            HttpServletRequest request
    ) {
        //checking if the user is root admin
        User user = userRepository.findById(actorId)
                .orElseThrow(() -> new RuntimeException("Actor not found."));

        if (!user.getRole().getRoleName().equals("ROOT_ADMIN")) {
            return ResponseEntity.status(403).body("Access Denied: Only Root Admin can create admin accounts.");
        }
         String ipAddress = IpAddressUtils.getClientIp(request);
        userService.createAdminAccount(createAdmin, user, ipAddress);

        return ResponseEntity.ok("Admin created successfully!");
    }



   // admin stats
    @GetMapping("/stats")
    public ResponseEntity<AdminStats> displayAdminStats(
    ){
        return ResponseEntity.ok(userService.displayAdminStats());
    }


   //admin table
    @GetMapping("admin-table")
    public ResponseEntity<Page<AdminTable>> displayAdminTable(

            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String role,
            @RequestParam(required = false) Status status
    ){
        return ResponseEntity.ok(userService.displayAllAdminTables(search,role,status,page,size));
    }




    // Update Admin
    @PutMapping("/update-admin")
    public ResponseEntity<String> updateAdminProfile(
            @RequestParam UUID userId,
            @Valid @RequestBody UpdateAdmin updateDto,
            @RequestParam UUID actorId,
            HttpServletRequest request
    ) {
        //check if user is root admin.
        User user = userRepository.findById(actorId).
                orElseThrow(() -> new RuntimeException("user not found."));

        if(!user.getRole().getRoleName().equals("ROOT_ADMIN")){
            throw new RuntimeException("Only root admin can access.");
        }

        String ipAddress = IpAddressUtils.getClientIp(request);

        userService.updateAdminAccount(userId, updateDto, user, ipAddress);
        return ResponseEntity.ok("Admin profile updated successfully and logged in audit trails.");
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
        User user = userDetails.user();

        userService.updateUserStatus(userId, status, user, ipAddress, actionRequest.reason());

        String actionName = (status == Status.ACTIVE) ? "restored" : "deactivated";
        return ResponseEntity.ok("User account has been successfully " + actionName + ".");
    }

}
