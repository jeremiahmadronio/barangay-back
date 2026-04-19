package com.barangay.barangay.security.database_backup.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class BackupStatsDTO {
    private Double storageUsedGb;
    private Double storageLimitGb;
    private String autoBackupFrequency; // e.g., "Daily", "Weekly"
    private String nextBackupTime;      // e.g., "Today at 00:00"
    private String lastBackupStatus;    // e.g., "Success"
    private String lastBackupDate;      // e.g., "Today"
}