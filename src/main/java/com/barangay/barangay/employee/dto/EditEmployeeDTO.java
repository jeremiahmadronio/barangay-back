package com.barangay.barangay.employee.dto;

import com.barangay.barangay.enumerated.Status;

public record EditEmployeeDTO (
        Long personId,
        Long departmentId,
        String position,
        Status status
) {
}
