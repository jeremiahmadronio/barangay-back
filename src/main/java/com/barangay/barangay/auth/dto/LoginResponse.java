package com.barangay.barangay.auth.dto;

import java.util.Set;
import java.util.UUID;

public record LoginResponse(
        String status,        // "MFA_REQUIRED" | "CHANGE_PASSWORD_REQUIRED" | "SUCCESS"
        UUID userId,
        String role,
        Set<String> departments,
        String token
){}
