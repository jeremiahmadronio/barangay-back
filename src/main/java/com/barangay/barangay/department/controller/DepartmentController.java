package com.barangay.barangay.department.controller;

import com.barangay.barangay.department.dto.DepartmentOptions;
import com.barangay.barangay.department.service.DepartmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/departments")
@RequiredArgsConstructor
public class DepartmentController {

    private final DepartmentService departmentService;

    @GetMapping("/options")
    public ResponseEntity<List<DepartmentOptions>> getDepartmentOptions() {
        return ResponseEntity.ok(departmentService.getAllDepartmentOptions());
    }

    @GetMapping("/admin-options")
    public ResponseEntity<List<DepartmentOptions>> getMyDepartments() {
        return ResponseEntity.ok(departmentService.getAssignedDepartmentOptions());
    }
}