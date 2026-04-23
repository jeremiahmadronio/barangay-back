package com.barangay.barangay.auth.dto;

import com.barangay.barangay.enumerated.MfaType;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record MfaRequest (
        @NotBlank @Email String email,
        @NotBlank @Size(min = 6, max = 6) String code,
        @NotNull MfaType type
){
}
