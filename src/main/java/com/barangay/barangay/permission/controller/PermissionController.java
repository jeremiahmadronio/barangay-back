package com.barangay.barangay.permission.controller;

import com.barangay.barangay.permission.dto.PermissionOptions;
import com.barangay.barangay.permission.service.PermissionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/permission")
@RequiredArgsConstructor
public class PermissionController {

    private final PermissionService permissionService;

    @GetMapping("/options")
    public ResponseEntity<List<PermissionOptions>> getPermissionOptions() {
        return ResponseEntity.ok(permissionService.getAllPermissionOptions());
    }
}
