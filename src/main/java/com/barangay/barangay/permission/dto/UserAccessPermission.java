package com.barangay.barangay.permission.dto;

import java.util.List;
import java.util.UUID;

public record UserAccessPermission (
        UUID userId,
        String username,
        String role,
        String department,
        List<String> permissions
){
}
