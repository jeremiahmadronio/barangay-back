    package com.barangay.barangay.users.dto;

    import java.math.BigDecimal;

    public record DeptActivityDTO(
            String departmentName,
            long count,
            BigDecimal percentage
    ) {
    }
