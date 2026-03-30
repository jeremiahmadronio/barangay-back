package com.barangay.barangay.resident.dto;

public record EmployeeResponseDTO(
        Long employeeId,
        String fullName,
        String position
) {
}
