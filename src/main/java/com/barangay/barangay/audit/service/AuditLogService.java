package com.barangay.barangay.audit.service;

import com.barangay.barangay.audit.dto.AuditFilterOptions;
import com.barangay.barangay.audit.dto.AuditTable;
import com.barangay.barangay.audit.dto.AuditViewAll;
import com.barangay.barangay.audit.dto.Stats;
import com.barangay.barangay.audit.model.AuditLog;
import com.barangay.barangay.audit.repository.AuditLogRepository;
import com.barangay.barangay.enumerated.Departments;
import com.barangay.barangay.enumerated.Severity;
import com.barangay.barangay.admin_management.model.User;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Service
@RequiredArgsConstructor
public class AuditLogService {

    private final AuditLogRepository auditLogRepository;
    private final ObjectMapper objectMapper;




    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void log(User actor, Departments dept, String module, Severity severity, String action, String ip, String reason, Object oldVal, Object newVal) {
        try {
            AuditLog log = AuditLog.builder()
                    .user(actor)
                    .department(dept)
                    .module(module)
                    .severity(severity)
                    .actionTaken(action)
                    .ipAddress(ip)
                    .reason(reason)
                    .oldValue(oldVal != null ? objectMapper.writeValueAsString(oldVal) : null)
                    .newValue(newVal != null ? objectMapper.writeValueAsString(newVal) : null)
                    .build();

            auditLogRepository.save(log);
        } catch (Exception e) {
            System.err.println("Failed to save audit log: " + e.getMessage());
        }
    }


    @Transactional(readOnly = true)
    public Stats getAuditSummary() {
        LocalDateTime startOfToday = LocalDateTime.now().with(LocalTime.MIN);
        return new Stats(
                auditLogRepository.countLogsToday(startOfToday),
                auditLogRepository.countAllLogs(),
                auditLogRepository.countWarningAlerts(),
                auditLogRepository.countCriticalAlerts()
        );
    }


    //TABLE
    public Page<AuditTable> getAuditTable(
            String search, String severity, String module, String action,
            LocalDate startDate, LocalDate endDate,
            int page, int size
    ) {
        String searchParam   = (search == null || search.isBlank()) ? null : "%" + search.toLowerCase() + "%";
        String severityParam = blankToNull(severity);
        String moduleParam   = blankToNull(module);
        String actionParam   = blankToNull(action);

        LocalDateTime startDateTime = (startDate != null) ? startDate.atStartOfDay() : null;
        LocalDateTime endDateTime   = (endDate   != null) ? endDate.atTime(LocalTime.MAX) : null;

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));

        return auditLogRepository
                .findAllFiltered(
                        searchParam, severityParam, moduleParam, actionParam,
                        startDateTime, endDateTime,
                        pageable
                )
                .map(this::toTable);
    }


    public AuditFilterOptions getFilterOptions() {
        return new AuditFilterOptions(
                auditLogRepository.findDistinctModules(),
                auditLogRepository.findDistinctActions(),
                auditLogRepository.findDistinctSeverities()
        );
    }

    private String blankToNull(String value) {
        return (value == null || value.isBlank()) ? null : value;
    }

    private AuditTable toTable(AuditLog log) {
        String firstName = log.getUser() != null ? log.getUser().getFirstName() : null;
        String lastName  = log.getUser() != null ? log.getUser().getLastName()  : null;
        String roleName  = (log.getUser() != null && log.getUser().getRole() != null)
                ? log.getUser().getRole().getRoleName()
                : null;

        return new AuditTable(
                log.getId(),
                firstName,
                lastName,
                roleName,
                log.getActionTaken(),
                log.getModule(),
                log.getReason(),
                log.getIpAddress(),
                log.getSeverity() != null ? log.getSeverity().name() : null
        );
    }


    @Transactional
    public AuditViewAll getAuditLog(Long id, User actor, String ipAddress) {
        AuditLog logRecord = auditLogRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new RuntimeException("Audit log not found: " + id));

        this.log(
                actor,
                Departments.ROOT_ADMIN,
                "ROOT_ADMIN",
                Severity.INFO,
                "VIEW_AUDITS_RECORD",
                ipAddress,
                "Viewed details of Audit Log  " + actor.getFirstName() + " " + actor.getLastName()
                ,
                null,
                null
        );

        return toViewAll(logRecord);
    }

    private AuditViewAll toViewAll(AuditLog log) {
        User user = log.getUser();

        return new AuditViewAll(
                log.getId(),
                user != null ? user.getFirstName() : null,
                user != null ? user.getLastName()  : null,
                (user != null && user.getRole() != null) ? user.getRole().getRoleName() : null,
                log.getIpAddress(),
                log.getModule(),
                log.getSeverity() != null ? log.getSeverity().name() : null,
                log.getActionTaken(),
                log.getReason(),
                log.getOldValue(),
                log.getNewValue(),
                log.getCreatedAt() != null ? log.getCreatedAt().toString() : null,
                log.getLastLoginAt() != null ? log.getLastLoginAt().toString() : null
        );
    }
}
