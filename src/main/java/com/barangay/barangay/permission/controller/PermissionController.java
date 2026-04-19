package com.barangay.barangay.permission.controller;

import com.barangay.barangay.permission.dto.PermissionOptions;
import com.barangay.barangay.permission.dto.UserAccessPermission;
import com.barangay.barangay.permission.service.PermissionService;
import com.barangay.barangay.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/permission")
@RequiredArgsConstructor
public class PermissionController {

    private final PermissionService permissionService;

    @GetMapping("/options")
    public ResponseEntity<List<PermissionOptions>> getPermissionOptions() {
        return ResponseEntity.ok(permissionService.getPermissionOptions());
    }


    @GetMapping("/my-access")
    public ResponseEntity<UserAccessPermission> getUserAccessPermission(
           @AuthenticationPrincipal CustomUserDetails customUserDetails
    ){
        return ResponseEntity.ok(permissionService.getMySecurityProfile(customUserDetails.user().getId()));

    }
}
