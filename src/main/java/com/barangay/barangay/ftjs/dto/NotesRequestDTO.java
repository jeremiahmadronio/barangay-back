package com.barangay.barangay.ftjs.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record NotesRequestDTO(

        @NotNull
        Long ftjsId,

        @NotBlank(message = "notes is required")
        String notes

) {
}
