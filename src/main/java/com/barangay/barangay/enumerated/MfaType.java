package com.barangay.barangay.enumerated;

public enum MfaType {
    EMAIL,          // Primary Email
    BACKUP_EMAIL,   // Backup Email
    TOTP,           // Authenticator App
    RECOVERY
}