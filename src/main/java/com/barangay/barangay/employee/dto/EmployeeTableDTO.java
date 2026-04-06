package com.barangay.barangay.employee.dto;

public record EmployeeTableDTO(
        Long id,
        String fullName,
        String email,
        String departmentName,
        String position,
        String status,
        long activeCases
) {
}
