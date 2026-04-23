package com.barangay.barangay.admin_management.dto;

import com.barangay.barangay.enumerated.MfaType;

import java.time.LocalDateTime;
import java.util.UUID;

public record UserSettingsPreview(
        UUID id,
        byte[] photo,
        String username,
        String firstName,
        String lastName,
        String systemEmail,
        String contactNumber,

        String roleName,
        String systemBackupEmail,
        MfaType mfaType,
        boolean totpEnabled,
        LocalDateTime createdAt,
        LocalDateTime lastLoginAt
) {
}
