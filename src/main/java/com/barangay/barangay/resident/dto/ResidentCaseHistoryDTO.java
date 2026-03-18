package com.barangay.barangay.resident.dto;

import java.time.LocalDateTime;

public record ResidentCaseHistoryDTO(
        String blotterNumber,
        String incidentNature,
        String role, // COMPLAINANT, RESPONDENT, or WITNESS
        String status,
        LocalDateTime dateFiled)
{}