package com.barangay.barangay.audit.service;

import com.barangay.barangay.audit.model.AuditLog;
import com.barangay.barangay.audit.repository.AuditLogRepository;
import com.barangay.barangay.auth.model.Department;
import com.barangay.barangay.users.model.User;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuditLogService {

    private final AuditLogRepository auditLogRepository;
    private final ObjectMapper objectMapper;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void log(User actor, Department dept, String module, String severity, String action, String ip, String reason, Object oldVal, Object newVal) {
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


}
