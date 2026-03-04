package com.barangay.barangay.dataseed;

import com.barangay.barangay.audit.model.AuditLog;
import com.barangay.barangay.audit.repository.AuditLogRepository;
import com.barangay.barangay.auth.model.Department;
import com.barangay.barangay.auth.model.Permission;
import com.barangay.barangay.auth.model.Role;
import com.barangay.barangay.users.model.User;
import com.barangay.barangay.auth.repository.DepartmentRepository;
import com.barangay.barangay.auth.repository.PermissionRepository;
import com.barangay.barangay.auth.repository.RoleRepository;
import com.barangay.barangay.users.repository.UserRepository;
import com.barangay.barangay.enumerated.Status;
import com.barangay.barangay.enumerated.Severity;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final PermissionRepository permissionRepository;
    private final DepartmentRepository departmentRepository;
    private final AuditLogRepository auditLogRepository;
    private final JdbcTemplate jdbcTemplate;

    @Override
    @Transactional
    public void run(String... args) {
        Department adminDept = createDeptIfNotFound("ADMINISTRATION");
        Department vawcDept = createDeptIfNotFound("VAWC");
        Department blotterDept = createDeptIfNotFound("BLOTTER");
        createDeptIfNotFound("KAPITANA");
        Department bcpcDept = createDeptIfNotFound("BCPC");
        Department clearanceDept = createDeptIfNotFound("CLEARANCE");
        createDeptIfNotFound("LUPONG TAGAPAMAYAPA");
        createDeptIfNotFound("OPERATIONAL STAFF");
        Department ftjsDept = createDeptIfNotFound("FTJS");
        createDeptIfNotFound("CONTENT");

        Permission allAccess = createPermIfNotFound("ALL_ACCESS");
        createPermIfNotFound("VIEW_RECORDS");
        createPermIfNotFound("CREATE_EDIT_RECORDS");
        createPermIfNotFound("DELETE_RECORDS_RESTRICTED");
        createPermIfNotFound("GENERATE_REPORTS");
        createPermIfNotFound("ISSUE_CERTIFICATES");

        Role rootRole = roleRepository.findByRoleName("ROOT_ADMIN")
                .orElseGet(() -> {
                    Role role = new Role();
                    role.setRoleName("ROOT_ADMIN");
                    role.setPermissions(Set.of(allAccess));
                    return roleRepository.save(role);
                });

        createRoleIfNotFound("ADMIN");
        createRoleIfNotFound("STAFF");

        User rootUser = userRepository.findByUsername("rootadmin")
                .orElseGet(() -> {
                    User root = new User();
                    root.setUsername("rootadmin");
                    root.setPassword("82219800Jeremiah");
                    root.setEmail("admin@ugong.gov.ph");
                    root.setFirstName("Juan");
                    root.setLastName("Dela Cruz");
                    root.setStatus(Status.ACTIVE);
                    root.setRole(rootRole);
                    root.setAllowedDepartments(new HashSet<>(Set.of(adminDept)));
                    root.setFailedAttempts(0);
                    root.setIsLocked(false);
                    return userRepository.save(root);
                });


        if (auditLogRepository.count() == 0) {
            System.out.println("Starting Audit Log Seeding for Dashboard Testing...");

            seedLogs(rootUser, vawcDept, "VAWC", 245);
            seedLogs(rootUser, blotterDept, "BLOTTER", 189);
            seedLogs(rootUser, bcpcDept, "BCPC", 156);
            seedLogs(rootUser, ftjsDept, "FTJS", 98);
            seedLogs(rootUser, clearanceDept, "CLEARANCE", 312);

            for (int i = 0; i < 3; i++) {
                auditLogRepository.save(AuditLog.builder()
                        .user(rootUser)
                        .severity(Severity.CRITICAL)
                        .module("SECURITY")
                        .actionTaken("UNAUTHORIZED_ACCESS_ATTEMPT")
                        .reason("Detected multiple failed attempts from unrecognized IP")
                        .ipAddress("192.168.1.50")
                        .build());
            }

            seedHistoricalLogs(rootUser, 50);

            System.out.println("Dashboard testing data seeded successfully.");
        }
    }

    private void seedLogs(User actor, Department dept, String module, int count) {
        for (int i = 0; i < count; i++) {
            auditLogRepository.save(AuditLog.builder()
                    .user(actor)
                    .department(dept)
                    .module(module)
                    .severity(Severity.INFO)
                    .actionTaken("CREATE_RECORD")
                    .reason("Initial migration data for " + module)
                    .ipAddress("127.0.0.1")
                    .build());
        }
    }
    private void seedHistoricalLogs(User actor, int count) {
        LocalDateTime lastMonth = LocalDateTime.now().minusMonths(1).minusDays(5);
        for (int i = 0; i < count; i++) {
            AuditLog log = AuditLog.builder()
                    .user(actor)
                    .severity(Severity.INFO)
                    .module("SYSTEM")
                    .actionTaken("HISTORICAL_LOG")
                    .reason("Log for monthly growth comparison")
                    .ipAddress("127.0.0.1")
                    .build();

            log = auditLogRepository.saveAndFlush(log);

            jdbcTemplate.update("UPDATE audit_logs SET created_at = ? WHERE id = ?", lastMonth, log.getId());
        }
    }

    private Department createDeptIfNotFound(String name) {
        return departmentRepository.findByName(name)
                .orElseGet(() -> {
                    Department dept = new Department();
                    dept.setName(name);
                    return departmentRepository.save(dept);
                });
    }

    private Permission createPermIfNotFound(String name) {
        return permissionRepository.findByPermissionName(name)
                .orElseGet(() -> {
                    Permission perm = new Permission();
                    perm.setPermissionName(name);
                    return permissionRepository.save(perm);
                });
    }

    private void createRoleIfNotFound(String name) {
        if (roleRepository.findByRoleName(name).isEmpty()) {
            Role role = new Role();
            role.setRoleName(name);
            roleRepository.save(role);
        }
    }
}