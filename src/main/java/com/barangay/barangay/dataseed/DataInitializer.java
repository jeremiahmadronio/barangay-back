package com.barangay.barangay.dataseed;

import com.barangay.barangay.audit.model.AuditLog;
import com.barangay.barangay.audit.repository.AuditLogRepository;
import com.barangay.barangay.department.model.Department;
import com.barangay.barangay.permission.model.Permission;
import com.barangay.barangay.role.model.Role;
import com.barangay.barangay.enumerated.Departments;
import com.barangay.barangay.admin_management.model.User;
import com.barangay.barangay.department.repository.DepartmentRepository;
import com.barangay.barangay.permission.repository.PermissionRepository;
import com.barangay.barangay.role.repository.RoleRepository;
import com.barangay.barangay.admin_management.repository.Root_AdminRepository;
import com.barangay.barangay.enumerated.Status;
import com.barangay.barangay.enumerated.Severity;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final RoleRepository roleRepository;
    private final Root_AdminRepository userRepository;
    private final PermissionRepository permissionRepository;
    private final DepartmentRepository departmentRepository;
    private final AuditLogRepository auditLogRepository;
    private final JdbcTemplate jdbcTemplate;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) {

        // ── Departments ──────────────────────────────────────────────────────
        Department adminDept = createDeptIfNotFound("ADMINISTRATION");
        createDeptIfNotFound("VAWC");
        createDeptIfNotFound("BLOTTER");
        createDeptIfNotFound("KAPITANA");
        createDeptIfNotFound("BCPC");
        createDeptIfNotFound("CLEARANCE");
        createDeptIfNotFound("LUPONG_TAGAPAMAYAPA");
        createDeptIfNotFound("OPERATIONAL_STAFF");
        createDeptIfNotFound("FTJS");
        createDeptIfNotFound("ROOT_ADMIN");
        createDeptIfNotFound("CONTENT");

        // ── Permissions ──────────────────────────────────────────────────────
        Permission allAccess = createPermIfNotFound("All Access");
        createPermIfNotFound("View Records");
        createPermIfNotFound("Edit Records");
        createPermIfNotFound("Create Records");
        createPermIfNotFound("Delete Records (Restricted)");
        createPermIfNotFound("Generate Reports");
        createPermIfNotFound("Issue Certificated");

        // ── Roles ────────────────────────────────────────────────────────────
        Role rootRole = roleRepository.findByRoleName("ROOT_ADMIN")
                .orElseGet(() -> {
                    Role role = new Role();
                    role.setRoleName("ROOT_ADMIN");
                    return roleRepository.save(role);
                });

        createRoleIfNotFound("ADMIN");
        createRoleIfNotFound("STAFF");


        String rawPassword = "82219800Jeremiah!";
        String hashedContext = passwordEncoder.encode(rawPassword);

        User rootUser = userRepository.findByUsername("rootadmin")
                .orElseGet(() -> {
                    User root = new User();
                    root.setUsername("rootadmin");
                    root.setPassword(hashedContext);
                    root.setEmail("nermamadronio@gmail.com");
                    root.setFirstName("Juan");
                    root.setLastName("Dela Cruz");
                    root.setStatus(Status.ACTIVE);
                    root.setRole(rootRole);
                    root.setAllowedDepartments(new HashSet<>(Set.of(adminDept)));
                    root.setFailedAttempts(0);
                    root.setIsLocked(false);
                    return userRepository.save(root);
                });

        // ── Audit Log Seeding ────────────────────────────────────────────────
        if (auditLogRepository.count() == 0) {
            System.out.println("Starting Audit Log Seeding for Dashboard Testing...");

            // FIX: Each seedLogs call now uses the correct department enum
            seedLogs(rootUser, Departments.VAWC,      "VAWC",      245);
            seedLogs(rootUser, Departments.BLOTTER,   "BLOTTER",   189);
            seedLogs(rootUser, Departments.BCPC,      "BCPC",      156);
            seedLogs(rootUser, Departments.FTJS,      "FTJS",       98);
            seedLogs(rootUser, Departments.CLEARANCE, "CLEARANCE", 312);

            // FIX: Added .department() — column is nullable = false, this would crash before
            List<AuditLog> criticalLogs = new ArrayList<>();
            for (int i = 0; i < 3; i++) {
                criticalLogs.add(AuditLog.builder()
                        .user(rootUser)
                        .department(Departments.ROOT_ADMIN)
                        .severity(Severity.CRITICAL)
                        .module("SECURITY")
                        .actionTaken("UNAUTHORIZED_ACCESS_ATTEMPT")
                        .reason("Detected multiple failed attempts from unrecognized IP")
                        .ipAddress("192.168.1.50")
                        .build());
            }
            auditLogRepository.saveAll(criticalLogs);

            seedHistoricalLogs(rootUser, 50);

            System.out.println("Dashboard testing data seeded successfully.");
        }
    }

    // ── Private Helpers ───────────────────────────────────────────────────────

    /**
     * FIX: Was hardcoding Departments.ROOT_ADMIN — now uses the dept param correctly.
     */
    private void seedLogs(User actor, Departments dept, String module, int count) {
        List<AuditLog> logsToSave = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            logsToSave.add(AuditLog.builder()
                    .user(actor)
                    .department(dept)
                    .module(module)
                    .severity(Severity.INFO)
                    .actionTaken("CREATE_RECORD")
                    .reason("Initial migration data for " + module)
                    .ipAddress("127.0.0.1")
                    .build());
        }
        auditLogRepository.saveAll(logsToSave);
    }

    /**
     * Seeds historical logs with a backdated created_at timestamp.
     */
    private void seedHistoricalLogs(User actor, int count) {
        // Use ROOT_ADMIN as the department for historical/system-generated logs
        Departments dept = Departments.ROOT_ADMIN;
        LocalDateTime lastMonth = LocalDateTime.now().minusMonths(1).minusDays(5);

        String roleName = (actor.getRole() != null)
                ? actor.getRole().getRoleName()
                : "SYSTEM";

        List<AuditLog> logsToSave = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            logsToSave.add(AuditLog.builder()
                    .user(actor)
                    .department(dept)
                    .severity(Severity.INFO)
                    .module(roleName)
                    .actionTaken("HISTORICAL_LOG")
                    .reason("Generated log for " + roleName)
                    .ipAddress("127.0.0.1")
                    .build());
        }

        // Save all first, then batch-update created_at to backdate them
        List<AuditLog> savedLogs = auditLogRepository.saveAll(logsToSave);

        List<Object[]> batchArgs = savedLogs.stream()
                .map(log -> new Object[]{lastMonth, log.getId()})
                .collect(Collectors.toList());

        jdbcTemplate.batchUpdate(
                "UPDATE audit_logs SET created_at = ? WHERE id = ?",
                batchArgs
        );
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