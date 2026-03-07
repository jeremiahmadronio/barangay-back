    package com.barangay.barangay.admin_management.dto;

    import java.math.BigDecimal;

    public record DeptActivityDTO(
            String departmentName,
            long count,
            BigDecimal percentage
    ) {
    }
