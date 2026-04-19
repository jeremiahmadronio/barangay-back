package com.barangay.barangay.ftjs.dto;

import com.barangay.barangay.enumerated.FtjsStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record StatusUpdateDTO (
        boolean isArchived,
        @NotBlank(message = "Remarks is required for tracking")
        String remarks
){
}
