package com.barangay.barangay.users.dto;

import jakarta.validation.constraints.NotBlank;

import java.time.LocalDateTime;

public record UserActionRequest(
        @NotBlank(message = "Reason is required for accountability")
        String reason,

        LocalDateTime lockUntil
) {
}
