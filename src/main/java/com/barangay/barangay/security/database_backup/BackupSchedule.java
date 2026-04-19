package com.barangay.barangay.security.database_backup;

import com.barangay.barangay.admin_management.model.User;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Table(name = "backup_schedules")
@Data
public class BackupSchedule {
    @Id
    private Long id = 1L;

    private String frequency;
    private Integer hour;
    private Integer minute;
    private String dayOfWeek;

    private boolean enabled = true;
    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "created_by")
    private User createdBy;
    private LocalDateTime createdAt;
}