package com.barangay.barangay.employee.dto;

import com.barangay.barangay.enumerated.Status;

public record UpdateEmployeeStatus (
        String reason,
        Status newStatus

) {
}
