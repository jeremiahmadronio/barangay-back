package com.barangay.barangay.clearance_management.dto;

import jakarta.validation.constraints.NotBlank;

public record ArchiveTemplateReponseDTO  (
        @NotBlank(message = "remarks is required")
        String remarks
){
}
