package com.barangay.barangay.user_management.dto;

import com.barangay.barangay.enumerated.Status;

import java.util.Set;

public record EditUserDTO(
        String username,
        String systemEmail,
        Long roleId,
        Set<Long> departmentIds,
        Set<Long> permissionIds,
        Status status
) {}