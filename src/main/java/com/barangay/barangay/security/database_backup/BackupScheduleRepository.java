package com.barangay.barangay.security.database_backup;

import org.springframework.data.jpa.repository.JpaRepository;

public interface BackupScheduleRepository extends JpaRepository<BackupSchedule, Long> {
}
