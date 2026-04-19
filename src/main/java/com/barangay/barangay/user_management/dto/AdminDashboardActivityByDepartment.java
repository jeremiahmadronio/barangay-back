package com.barangay.barangay.user_management.dto;

import java.time.LocalDate;

public record AdminDashboardActivityByDepartment(
        String departmentName,
        Long count,
        Double percentage
) {
}
