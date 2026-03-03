package com.barangay.barangay.users.dto;

import java.util.List;

public record ActivityOverviewDTO(
        long totalActivity,
        List<DeptActivityDTO> departments
) {}