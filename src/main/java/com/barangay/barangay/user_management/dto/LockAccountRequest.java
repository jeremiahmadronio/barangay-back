package com.barangay.barangay.user_management.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public record LockAccountRequest(
        @NotNull LocalDateTime lockUntil,
        @NotBlank String reason
) {
}
