package com.barangay.barangay.lupon.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record PangkatMemberDTO (

        @NotNull
        Long employeeId,
        @NotBlank
        String firstName,
        @NotBlank
        String lastName,
        @NotBlank
        String position
) {
}
