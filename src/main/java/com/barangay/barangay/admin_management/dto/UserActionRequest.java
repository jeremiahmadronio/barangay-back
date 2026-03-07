package com.barangay.barangay.admin_management.dto;

import jakarta.validation.constraints.NotBlank;

import java.time.LocalDateTime;

public record UserActionRequest(
        @NotBlank(message = "Reason is required for accountability")
        String reason,

        LocalDateTime lockUntil
) {
}
