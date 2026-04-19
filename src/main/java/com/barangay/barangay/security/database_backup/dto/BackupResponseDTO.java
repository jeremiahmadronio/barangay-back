package com.barangay.barangay.security.database_backup.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class BackupResponseDTO {
    private Long id;
    private String fileName;
    private String label;
    private String createdBy;
    private LocalDateTime createdAt;
    private Long fileSizeKb;
    private boolean isEncrypted;
}