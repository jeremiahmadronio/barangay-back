package com.barangay.barangay.ftjs.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;

public record RequestNewFtjsDTO(
        @NotNull Long ftjsId,
        @NotBlank String reason,
        @NotEmpty byte[] affidavitFiles,
        @NotNull LocalDate dateOfLoss,
        String orNumber,
        BigDecimal amountPaid
) {
}
