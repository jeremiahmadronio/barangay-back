package com.barangay.barangay.admin_management.dto;

import java.util.UUID;

public record UserSettings(
        UUID id,
        String username,
        String password,
        String email,
        String firstName,
        String lastName,
        String contactNumber

) {
}
