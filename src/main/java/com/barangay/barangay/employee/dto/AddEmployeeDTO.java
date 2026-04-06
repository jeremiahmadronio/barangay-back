package com.barangay.barangay.employee.dto;


import com.barangay.barangay.enumerated.Status;

public record AddEmployeeDTO(
        Long personId,
        boolean isGlobal,
        Long departmentId,
        String position,
        Status status
) {
}
