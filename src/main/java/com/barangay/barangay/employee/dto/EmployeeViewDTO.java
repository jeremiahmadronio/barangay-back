package com.barangay.barangay.employee.dto;

import com.barangay.barangay.enumerated.Status;

import java.time.LocalDate;
import java.util.List;

public record EmployeeViewDTO (
        Long id,
        String full_name,
        byte[] photo,
        Status status,
        String statusRemarks,
        String email,
        String contactNumber,
        LocalDate birthDate,
        Short age,
        String gender,
        String civilStatus,
        String completeAddress,
        String assignDepartment,
        String position,
        List<EmployeeAssignCaseDTO> assignCase
) {
}
