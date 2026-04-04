package com.barangay.barangay.employee.dto;

public record EmployeeRequest(
        Long personId,
        String firstName,
        String lastName,
        Long departmentId,
        String position
) {
}
