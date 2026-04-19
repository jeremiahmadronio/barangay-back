package com.barangay.barangay.lupon.dto;

import jakarta.validation.constraints.NotBlank;

public record ExtendLuponRequest(
        @NotBlank(message = "A reason for the extension is mandatory.")
        String reason
) {
}
