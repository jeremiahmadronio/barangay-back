package com.barangay.barangay.employee.dto;

import java.time.LocalDateTime;

public record EmployeeAssignCaseDTO(
        Long id,
        String caseNumber,
        String natureOfComplaint,
        String status,
        LocalDateTime caseFiledAt,
        String complainantFullName
) {
}
