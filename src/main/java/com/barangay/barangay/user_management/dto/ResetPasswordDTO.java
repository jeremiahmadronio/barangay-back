package com.barangay.barangay.user_management.dto;

import jakarta.validation.constraints.NotBlank;

public record ResetPasswordDTO(
        @NotBlank(message = "Reason is required for audit purposes")
        String reason
) {
}
