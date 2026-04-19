package com.barangay.barangay.admin_management.dto;

import jakarta.validation.constraints.*;
import java.util.Set;

public record CreateAdmin (

        Long personId,

        String systemEmail,

        Set<Long> departmentIds,

        Set<Long> permissionsIds,

        boolean activateImmediately
) {
}