package com.barangay.barangay.blotter.dto.complaint;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ArchiveCaseDTO(

        @NotBlank(message = "Reason for archiving is required.")
        @Size(min = 1, message = "Please provide a more detailed reason.")
        String reason
) {
}
