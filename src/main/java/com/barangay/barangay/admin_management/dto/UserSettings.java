package com.barangay.barangay.admin_management.dto;


public record UserSettings(
        String firstName,
        String lastName,
        String contactNumber,
        byte[] photo,
        String username,
        String systemEmail,
        String systemBackupEmail,
        String currentPassword,
        String newPassword

) {
}
