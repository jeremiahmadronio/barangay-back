package com.barangay.barangay.security.database_backup;

import jakarta.validation.constraints.NotBlank;

public record TriggerBackupDTO (
        @NotBlank(message = "reason is required")
        String reason
) {
}
