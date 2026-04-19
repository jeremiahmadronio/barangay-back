package com.barangay.barangay.security.database_backup;

import com.barangay.barangay.audit.service.AuditLogService;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.concurrent.ScheduledFuture;

@Service
@RequiredArgsConstructor
@Slf4j
public class ScheduleBackupService {




    private final TaskScheduler taskScheduler;
    private final BackupScheduleRepository scheduleRepository;
    private final DatabaseBackupService backupService; // Ang existing service mo

    private ScheduledFuture<?> scheduledTask;


    @PostConstruct
    public void init() {
        refreshSchedule();
    }

    public void refreshSchedule() {
        if (scheduledTask != null) {
            scheduledTask.cancel(false);
            log.info("DYNAMIC SCHEDULER: Old task cancelled.");
        }

        LocalDateTime now = LocalDateTime.now();

        BackupSchedule config = scheduleRepository.findById(1L)
                .orElseGet(() -> {
                    BackupSchedule def = new BackupSchedule();
                    def.setFrequency("DAILY");
                    def.setHour(0);
                    def.setMinute(0);
                    def.setCreatedAt(now);
                    return scheduleRepository.save(def);
                });

        if (!config.isEnabled()) return;

        String cronExpression = buildCron(config);
        log.info("DYNAMIC SCHEDULER: Setting new cron -> {}", cronExpression);

        scheduledTask = taskScheduler.schedule(() -> {
            log.info("AUTO-EXECUTE: Running scheduled backup based on Admin Choice.");
            backupService.performScheduledBackup();
        }, new CronTrigger(cronExpression));
    }

    private String buildCron(BackupSchedule config) {
        if ("WEEKLY".equalsIgnoreCase(config.getFrequency())) {
            return String.format("0 %d %d * * %s",
                    config.getMinute(), config.getHour(), config.getDayOfWeek());
        }
        // Default: Daily
        return String.format("0 %d %d * * *", config.getMinute(), config.getHour());
    }
}
