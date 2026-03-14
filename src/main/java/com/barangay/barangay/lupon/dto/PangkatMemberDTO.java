package com.barangay.barangay.lupon.dto;

import jakarta.validation.constraints.NotBlank;

public record PangkatMemberDTO (
        @NotBlank
        String firstName,
        @NotBlank
        String lastName,
        @NotBlank
        String position
) {
}
