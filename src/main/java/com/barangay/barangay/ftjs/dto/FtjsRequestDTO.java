package com.barangay.barangay.ftjs.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

public record FtjsRequestDTO(
        Long resident_id,

        String firstName,
        String lastName,
        String gender,
        String address,
        String contactNumber,
        String email,
            String schoolAddress,

        @NotBlank(message = "Educational attainment is required")
        String educationalAttainment,

        String course,
        String institution,

        String validIdType,
        String idNumber,

        @NotEmpty(message = "Oath of Undertaking file is required")
        byte[] oathFiles,

        @NotBlank(message = "Purpose is required")
        String purpose


) {
}
