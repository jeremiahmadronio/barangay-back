package com.barangay.barangay.user_management.service;

import com.barangay.barangay.audit.service.AuditLogService;
import com.barangay.barangay.department.repository.DepartmentRepository;
import com.barangay.barangay.enumerated.Departments;
import com.barangay.barangay.enumerated.Severity;
import com.barangay.barangay.enumerated.Status;
import com.barangay.barangay.permission.repository.PermissionRepository;
import com.barangay.barangay.person.model.Person;
import com.barangay.barangay.person.repository.PersonRepository;
import com.barangay.barangay.role.model.Role;
import com.barangay.barangay.role.repository.RoleRepository;
import com.barangay.barangay.user_management.dto.*;
import com.barangay.barangay.user_management.repository.UserManagementRepository;
import com.barangay.barangay.department.model.Department;
import com.barangay.barangay.admin_management.model.User;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.barangay.barangay.permission.model.Permission;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserManagementService {

    private final UserManagementRepository userManagementRepository;

    private final RoleRepository roleRepository;
    private final DepartmentRepository departmentRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuditLogService auditLogService;
    private final PermissionRepository permissionRepository;
    private final PersonRepository personRepository;
    private final ObjectMapper objectMapper;
    private final SendCredentials sendCredentials;




    @Transactional(readOnly = true)
    public UserStats getGlobalDashboardStats() {
        List<String> excludedRoles = List.of("ROOT_ADMIN", "ADMIN");

        return new UserStats(
                null,
                userManagementRepository.countAllGlobal(excludedRoles),
                userManagementRepository.countActiveGlobal(excludedRoles),
                userManagementRepository.countInactiveGlobal(excludedRoles),
                userManagementRepository.countLockedGlobal(excludedRoles)
        );
    }


    @Transactional(readOnly = true)
    public UserStats getDashboardStats(User currentAdmin) {

        User managedAdmin = userManagementRepository.findById(currentAdmin.getId())
                .orElseThrow(() -> new RuntimeException("Admin not found"));

        Set<Long> deptIds = managedAdmin.getAllowedDepartments().stream()
                .map(Department::getId)
                .collect(Collectors.toSet());

        if (deptIds.isEmpty()) {
            return new UserStats(managedAdmin.getId(), 0L, 0L, 0L, 0L);
        }

        List<String> excludedRoles = List.of("ROOT_ADMIN", "ADMIN");
        UUID currentUserId = managedAdmin.getId();

        return new UserStats(
                managedAdmin.getId(),
                userManagementRepository.countUsersByDepartments(deptIds, currentUserId, excludedRoles),
                userManagementRepository.countActiveUsersByDepartments(deptIds, currentUserId, excludedRoles),
                userManagementRepository.countInactiveUsersByDepartments(deptIds, currentUserId, excludedRoles),
                userManagementRepository.countLockedUsersByDepartments(deptIds, currentUserId, excludedRoles)
        );
    }



    @Transactional
    public void createUserAccount(CreateUser request, User officer, String ipAddress) {
        // 1. Fetch Person & Validations
        Person person = personRepository.findById(request.personId())
                .orElseThrow(() -> new RuntimeException("Resource Not Found: Person record not found."));

        if (userManagementRepository.existsByPerson(person)) {
            throw new RuntimeException("Conflict: This person already has a linked system account.");
        }

        if(userManagementRepository.existsByUsername(request.username())) {
            throw new RuntimeException("Conflict: Username already exists.");
        }
        if(userManagementRepository.existsBySystemEmail(request.systemEmail())) {
            throw new RuntimeException("Conflict: System email already exists.");
        }

        // 2. Password logic
        String rawPassword = PasswordGenerator.generateRandomPassword(12);
        String encodedPassword = passwordEncoder.encode(rawPassword);

        // 3. User Mapping
        User user = new User();
        user.setPerson(person);
        user.setUsername(request.username());
        user.setPassword(encodedPassword);
        user.setSystemEmail(request.systemEmail());
        user.setStatus(request.activateImmediately() ? Status.ACTIVE : Status.INACTIVE);
        user.setNewAccount(true);

        Role role = roleRepository.findById(request.roleId())
                .orElseThrow(() -> new RuntimeException("Resource Not Found: Role ID " + request.roleId() + " not found."));
        user.setRole(role);

        Set<Department> depts = new HashSet<>(departmentRepository.findAllById(request.departmentIds()));
        user.setAllowedDepartments(depts);


        if (request.permissionIds() != null && !request.permissionIds().isEmpty()) {
            Set<Permission> perms = new HashSet<>(permissionRepository.findAllById(request.permissionIds()));
            user.setCustomPermissions(perms);
        }

        // 7. Save and Audit
        User savedUser = userManagementRepository.save(user);
        logUserCreationAudit(savedUser, officer, ipAddress);

        String fullName = person.getFirstName() + " " + person.getLastName();

        // 8. Communication
        sendCredentials.sendCredentials(request.systemEmail(), fullName, rawPassword);
    }
    private void logUserCreationAudit(User newUser, User officer, String ip) {
        try {
            Map<String, Object> snapshot = new LinkedHashMap<>();
            snapshot.put("newUserId", newUser.getId());
            snapshot.put("username", newUser.getUsername());
            snapshot.put("fullName", newUser.getPerson().getFirstName() + " " + newUser.getPerson().getLastName());
            snapshot.put("role", newUser.getRole().getRoleName());
            snapshot.put("assignedDepartments", newUser.getAllowedDepartments().stream().map(Department::getName).toList());
            snapshot.put("isNewAccount", newUser.isNewAccount());

            String jsonState = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(snapshot);

            auditLogService.log(
                    officer,
                    Departments.ADMINISTRATION,
                    "User Management",
                    Severity.INFO,
                    "Register new user — @" + newUser.getUsername(),
                    ip,
                    null,
                    null,
                    jsonState
            );
        } catch (Exception e) {
            System.err.println("Audit Log Failure: " + e.getMessage());
        }
    }



        @Transactional(readOnly = true)
        public Page<UserTable> getStaffTable(User currentAdmin, Pageable pageable,
                                             String search, String roleName, String departmentName) {

            User managedAdmin = userManagementRepository.findByIdWithDepartments(currentAdmin.getId())
                    .orElseThrow(() -> new RuntimeException("Admin context not found."));

            Set<Long> deptIds = managedAdmin.getAllowedDepartments().stream()
                    .map(Department::getId)
                    .collect(Collectors.toSet());

            if (deptIds.isEmpty()) return Page.empty(pageable);

            String searchParam = (search != null && !search.isBlank()) ? "%" + search.trim() + "%" : null;

            String roleParam = (roleName != null && !roleName.isBlank() && !roleName.equalsIgnoreCase("All Roles"))
                    ? roleName : null;

            String deptParam = (departmentName != null && !departmentName.isBlank() && !departmentName.equalsIgnoreCase("All Departments"))
                    ? departmentName : null;

            List<String> excludedRoles = List.of("ROOT_ADMIN", "ADMIN");

            Page<User> users = userManagementRepository.findStaffByFilters(
                    deptIds,
                    currentAdmin.getId(),
                    excludedRoles,
                    searchParam,
                    roleParam,
                    deptParam,
                    pageable
            );

            // 4. Map to DTO
            return users.map(this::mapToUserTable);
        }

        private UserTable mapToUserTable(User user) {
            return new UserTable(
                    user.getId(),
                    user.getPerson().getPhoto(),
                    user.getUsername(),
                    user.getPerson().getFirstName(),
                    user.getPerson().getLastName(),
                    user.getRole() != null ? user.getRole().getRoleName() : "N/A",
                    user.getAllowedDepartments().stream()
                            .map(Department::getName)
                            .collect(Collectors.joining(", ")),
                    user.getCustomPermissions().stream()
                            .map(Permission::getPermissionName)
                            .collect(Collectors.toSet()),
                    Boolean.TRUE.equals(user.getIsLocked()),
                    user.getStatus().name(),
                    user.getStatusRemarks(),
                    user.getLastLoginAt()
            );
        }



    @Transactional(readOnly = true)
    public Page<UserTable> getGlobalStaffTable(User currentAdmin, Pageable pageable,
                                               String search, String roleName, String departmentName) {

        // 1. Prepare Params (Standardized Logic)
        String searchParam = (search != null && !search.isBlank()) ? "%" + search.trim() + "%" : null;
        String roleParam = (roleName != null && !roleName.isBlank() && !roleName.equalsIgnoreCase("All Roles"))
                ? roleName : null;
        String deptParam = (departmentName != null && !departmentName.isBlank() && !departmentName.equalsIgnoreCase("All Departments"))
                ? departmentName : null;

        List<String> excludedRoles = List.of("ROOT_ADMIN", "ADMIN");

        // 2. Fetch from Repo (Global Mode - No deptIds passed)
        Page<User> users = userManagementRepository.findGlobalStaffByFilters(
                currentAdmin.getId(),
                excludedRoles,
                searchParam,
                roleParam,
                deptParam,
                pageable
        );

        // 3. Map to DTO using your existing helper
        return users.map(this::mapToUserTable);
    }


    @Transactional
    public void updateUserAccount(UUID userId, EditUserDTO request, User officer, String ipAddress) {
        User user = userManagementRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Resource Not Found: User not found."));

        Map<String, Object> oldValues = new HashMap<>();
        Map<String, Object> newValues = new HashMap<>();

        if (request.username() != null && !request.username().equals(user.getUsername())) {
            if (userManagementRepository.existsByUsername(request.username())) {
                throw new RuntimeException("Conflict: Username already taken.");
            }
            oldValues.put("username", user.getUsername());
            user.setUsername(request.username());
            newValues.put("username", user.getUsername());
        }

        String fullName = user.getPerson().getFirstName() + " " + user.getPerson().getLastName();

        if (request.systemEmail() != null && !request.systemEmail().equals(user.getSystemEmail())) {
            if (userManagementRepository.existsBySystemEmail(request.systemEmail())) {
                throw new RuntimeException("Conflict: System email already in use.");
            }
            oldValues.put("systemEmail", user.getSystemEmail());
            user.setSystemEmail(request.systemEmail());
            newValues.put("systemEmail", user.getSystemEmail());

            // Generate bagong password — huwag i-send yung hashed password
            String newRawPassword = PasswordGenerator.generateRandomPassword(12);
            user.setPassword(passwordEncoder.encode(newRawPassword));
            user.setNewAccount(true); // force password change on next login
            sendCredentials.sendCredentials(request.systemEmail(), fullName, newRawPassword);
        }

        if (request.roleId() != null && (user.getRole() == null || !request.roleId().equals(user.getRole().getId()))) {
            Role newRole = roleRepository.findById(request.roleId())
                    .orElseThrow(() -> new RuntimeException("Resource Not Found: Role not found."));
            oldValues.put("role", user.getRole() != null ? user.getRole().getRoleName() : "NONE");
            user.setRole(newRole);
            newValues.put("role", newRole.getRoleName());
        }

        if (request.status() != null && !request.status().equals(user.getStatus())) {
            oldValues.put("status", user.getStatus());
            user.setStatus(request.status());
            newValues.put("status", user.getStatus());
        }

        if (request.departmentIds() != null) {
            oldValues.put("departments", user.getAllowedDepartments().stream().map(Department::getName).toList());
            Set<Department> newDepts = new HashSet<>(departmentRepository.findAllById(request.departmentIds()));
            user.setAllowedDepartments(newDepts);
            newValues.put("departments", newDepts.stream().map(Department::getName).toList());
        }

        if (request.permissionIds() != null) {
            oldValues.put("permissions", user.getCustomPermissions().stream().map(Permission::getPermissionName).toList());
            Set<Permission> newPerms = new HashSet<>(permissionRepository.findAllById(request.permissionIds()));
            user.setCustomPermissions(newPerms);
            newValues.put("permissions", newPerms.stream().map(Permission::getPermissionName).toList());
        }

        if (oldValues.isEmpty()) return;

        userManagementRepository.save(user);

        auditLogService.log(
                officer,
                Departments.ADMINISTRATION,
                "User Management",
                Severity.INFO,
                "User details Updated — @" + officer.getUsername() + ".",
                ipAddress,
                null,
                oldValues,
                newValues
        );
    }



    @Transactional
    public void resetUserPassword(UUID userId, ResetPasswordDTO request, User officer, String ipAddress) {
        User user = userManagementRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Resource Not Found: User not found."));

        String newRawPassword = PasswordGenerator.generateRandomPassword(12);
        String encodedPassword = passwordEncoder.encode(newRawPassword);

        user.setPassword(encodedPassword);
        user.setNewAccount(true);
        userManagementRepository.save(user);

        try {
            auditLogService.log(
                    officer,
                    Departments.ADMINISTRATION,
                    "User Management",
                    Severity.WARNING,
                    "Reset user password — @"  +  user.getUsername() + ".",
                    ipAddress,
                     request.reason(),
                    null,
                    null
            );
        } catch (Exception e) {
            System.err.println("Audit Log Failed: " + e.getMessage());
        }

        String fullName = user.getPerson().getFirstName() + " " + user.getPerson().getLastName();

        sendCredentials.sendCredentials(user.getSystemEmail(), fullName, newRawPassword);

    }


    @Transactional(readOnly = true)
    public UserViewDTO getUserFullDetails(UUID userId) {
        User user = userManagementRepository.findByIdWithFullDetails(userId)
                .orElseThrow(() -> new RuntimeException("Resource Not Found: User not found."));

        Person person = user.getPerson();

        return new UserViewDTO(
                person.getFirstName() + " " + person.getLastName(),
                user.getUsername(),
                person.getContactNumber(),
                user.getSystemEmail(),
                person.getAge(),
                person.getGender(),
                person.getCivilStatus(),
                person.getCompleteAddress(),
                user.getRole() != null ? user.getRole().getRoleName() : "N/A",
                user.getAllowedDepartments().stream()
                        .map(Department::getName)
                        .collect(Collectors.joining(", ")),
                user.getCustomPermissions().stream()
                        .map(Permission::getPermissionName)
                        .collect(Collectors.toSet()),
                user.getStatus().name(),
                user.getCreatedAt(),
                user.getUpdatedAt(),
                user.getLastLoginAt()
        );
    }





    @Transactional
    public void updateUserStatus(UUID userId, UpdateStatusRequest request, User officer, String ipAddress) {
        User user = userManagementRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found."));

        String oldStatus = user.getStatus().name();
        user.setStatus(request.newStatus());
        user.setStatusRemarks(request.remarks());

        userManagementRepository.save(user);

        auditLogService.log(officer, Departments.ADMINISTRATION,
                "User Management", Severity.INFO,
                "Update Status — @" + officer.getUsername() + "."  ,
                ipAddress,
                request.remarks(),
                oldStatus, request.newStatus().name());
    }

    @Transactional
    public void lockUserAccount(UUID userId, LockAccountRequest request, User officer, String ipAddress) {
        User user = userManagementRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found."));

        user.setIsLocked(true);
        user.setLockUntil(request.lockUntil());
        user.setStatusRemarks("Locked by Admin: " + request.reason());

        userManagementRepository.save(user);

        auditLogService.log(officer, Departments.ADMINISTRATION, "User Management", Severity.WARNING,

                "Lock user Account — @" + user.getUsername() + "."  ,
                ipAddress,
                request.reason(),
                "isLocked: false", "isLocked: true");
    }

    @Transactional
    public void unlockUserAccount(UUID userId, User officer, String ipAddress) {
        User user = userManagementRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found."));

        user.setIsLocked(false);
        user.setLockUntil(null);
        user.setFailedAttempts(0);
        userManagementRepository.save(user);

        auditLogService.log(officer, Departments.ADMINISTRATION, "USER_MANAGEMENT", Severity.INFO,
                "Unlock user Account — @" +  user.getUsername() + ".",
                 ipAddress,
                null,
                "isLocked: true", "isLocked: false");
    }



}
