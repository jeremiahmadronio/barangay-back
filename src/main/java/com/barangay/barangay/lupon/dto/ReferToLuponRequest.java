package com.barangay.barangay.lupon.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import java.util.List;

public record ReferToLuponRequest (
        @NotEmpty @Size(min = 3, max = 3)
        List<PangkatMemberDTO> members
) {
}
