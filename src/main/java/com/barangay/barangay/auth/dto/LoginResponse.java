package com.barangay.barangay.auth.dto;

import java.util.Set;
import java.util.UUID;

public record LoginResponse(
        String status,
        UUID userId,
        String role,
        Set<String> departments,
        String token,
        boolean totpEnabled,
        boolean hasBackupEmail
){}
