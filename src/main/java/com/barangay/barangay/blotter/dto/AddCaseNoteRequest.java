package com.barangay.barangay.blotter.dto;

import jakarta.validation.constraints.NotBlank;

public record AddCaseNoteRequest(
        @NotBlank String blotterNumber,
        @NotBlank String note
) {
}
