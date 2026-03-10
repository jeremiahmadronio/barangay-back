package com.barangay.barangay.role.controller;

import com.barangay.barangay.role.dto.RoleOptions;
import com.barangay.barangay.role.service.RoleService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/roles")
@RequiredArgsConstructor
public class RoleController {

    private final RoleService roleService;

    @GetMapping("/admin-options")
    public ResponseEntity<List<RoleOptions>> getAdminOptions() {
        return ResponseEntity.ok(roleService.getAdminRoleOptions());
    }

    @GetMapping("/staff-options")
    public ResponseEntity<List<RoleOptions>> getStaffRoleOptions() {
        return ResponseEntity.ok(roleService.getStaffRoleOptions());
    }
}