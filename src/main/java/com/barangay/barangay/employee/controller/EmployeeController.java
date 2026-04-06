package com.barangay.barangay.employee.controller;

import com.barangay.barangay.audit.service.IpAddressUtils;
import com.barangay.barangay.employee.dto.*;
import com.barangay.barangay.employee.model.Employee;
import com.barangay.barangay.employee.service.EmployeeService;
import com.barangay.barangay.enumerated.Status;
import com.barangay.barangay.security.CustomUserDetails;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/employee")
@RequiredArgsConstructor
public class EmployeeController {

    private final EmployeeService employeeService;


    @PostMapping("/hire")
    public ResponseEntity<?> hireEmployee(
            @Valid @RequestBody AddEmployeeDTO request,
            @AuthenticationPrincipal CustomUserDetails officer,
            HttpServletRequest servletRequest
    ) {
        String ipAddress = IpAddressUtils.getClientIp(servletRequest);
         employeeService.hireEmployee(
                request,
                officer.user(),
                ipAddress
        );
        return ResponseEntity.ok("Employee successfully created and linked.");    }


    @GetMapping("/stats")
    public ResponseEntity<EmployeeStatsDTO> getScopedVawcStats(
            @AuthenticationPrincipal CustomUserDetails officerDetails
    ) {

        UUID userId = officerDetails.user().getId();

        EmployeeStatsDTO stats = employeeService.getScopedEmployeeStats(userId);

        return ResponseEntity.ok(stats);
    }


    @GetMapping("/paged-table")
    public ResponseEntity<Page<EmployeeTableDTO>> getEmployees(
            @AuthenticationPrincipal CustomUserDetails adminDetails,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Long deptId,
            @RequestParam(required = false) Status status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);

        Page<EmployeeTableDTO> result = employeeService.getPaginatedEmployees(
                adminDetails.user().getId(),
                search,
                deptId,
                status,
                pageable
        );

        return ResponseEntity.ok(result);
    }


    @GetMapping("/view/{id}")
    public ResponseEntity<EmployeeViewDTO> getEmployeeProfile(@PathVariable Long id) {
        return ResponseEntity.ok(employeeService.getEmployeeFullDetails(id));
    }

    @PutMapping("/update-status/{id}")
    public ResponseEntity<?> updateEmployeeStatus(
            @PathVariable Long id,
        @RequestBody @Valid UpdateEmployeeStatus dto,
            @AuthenticationPrincipal CustomUserDetails officer,
            HttpServletRequest servletRequest
    ){

        String ipAddress = IpAddressUtils.getClientIp(servletRequest);
        employeeService.updateEmployeeStatus(id, dto, officer.user(), ipAddress);

        return ResponseEntity.ok("Successfully updated employee status");

    }



    @PutMapping("/edit-employee/{id}")
    public ResponseEntity<?> editEmployee(
            @PathVariable Long id,
            @Valid @RequestBody EditEmployeeDTO request,
            @AuthenticationPrincipal CustomUserDetails officerDetails,
            HttpServletRequest servletRequest
    ) {
        String ipAddress = IpAddressUtils.getClientIp(servletRequest);

         employeeService.editEmployee(
                id,
                request,
                officerDetails.user(),
                ipAddress
        );

        return ResponseEntity.ok("Successfully update Employee");
    }


}
