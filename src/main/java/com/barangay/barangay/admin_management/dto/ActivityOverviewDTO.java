package com.barangay.barangay.admin_management.dto;

import java.util.List;

public record ActivityOverviewDTO(
        long totalActivity,
        List<DeptActivityDTO> departments
) {}