package com.barangay.barangay.admin_management.dto;

import java.util.UUID;

public record UserSettingsPreview(
        UUID id,
        String username,

        String email,
        String firstName,
        String lastName,
        String contactNumber
) {
}
