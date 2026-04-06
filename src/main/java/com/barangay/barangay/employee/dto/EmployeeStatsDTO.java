package com.barangay.barangay.employee.dto;

public record EmployeeStatsDTO(
        long totalEmployees,
        long activeOfficers,
        long inactiveStaff,
        long totalDepartments
) {
}
