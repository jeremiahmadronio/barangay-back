package com.barangay.barangay.person.dto;

public record EmployeeResponseDTO(
        Long employeeId,
        String fullName,
        String position
) {
}
