package com.barangay.barangay.employee.controller;

import com.barangay.barangay.audit.service.IpAddressUtils;
import com.barangay.barangay.employee.dto.EmployeeRequest;
import com.barangay.barangay.employee.model.Employee;
import com.barangay.barangay.employee.service.EmployeeService;
import com.barangay.barangay.security.CustomUserDetails;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/employee")
@RequiredArgsConstructor
public class EmployeeController {

    private final EmployeeService employeeService;


    @PostMapping("/hire")
    public ResponseEntity<Employee> hireEmployee(
            @Valid @RequestBody EmployeeRequest request,
            @AuthenticationPrincipal CustomUserDetails officer,
            HttpServletRequest servletRequest
    ) {
        String ipAddress = IpAddressUtils.getClientIp(servletRequest);
        Employee newEmployee = employeeService.hireEmployee(
                request.personId(),
                request.departmentId(),
                request.position(),
                officer.user(),
                ipAddress
        );
        return ResponseEntity.ok(newEmployee);
    }
}
