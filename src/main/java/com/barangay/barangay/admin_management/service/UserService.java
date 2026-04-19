package com.barangay.barangay.admin_management.service;

import com.barangay.barangay.audit.service.AuditLogService;
import com.barangay.barangay.department.model.Department;
import com.barangay.barangay.permission.model.Permission;
import com.barangay.barangay.permission.repository.PermissionRepository;
import com.barangay.barangay.person.model.Person;
import com.barangay.barangay.person.repository.PersonRepository;
import com.barangay.barangay.role.model.Role;
import com.barangay.barangay.department.repository.DepartmentRepository;
import com.barangay.barangay.role.repository.RoleRepository;
import com.barangay.barangay.enumerated.Departments;
import com.barangay.barangay.enumerated.Severity;
import com.barangay.barangay.enumerated.Status;
import com.barangay.barangay.admin_management.dto.*;
import com.barangay.barangay.admin_management.model.User;
import com.barangay.barangay.admin_management.repository.Root_AdminRepository;
import com.barangay.barangay.user_management.repository.UserManagementRepository;
import com.barangay.barangay.user_management.service.PasswordGenerator;
import com.barangay.barangay.user_management.service.SendCredentials;
import com.barangay.barangay.user_management.service.UserManagementService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {


    private final DepartmentRepository departmentRepository;
    private final Root_AdminRepository userRepository;
    private final UserManagementRepository userManagementRepository;
    private final PermissionRepository permissionRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuditLogService auditLogService;
    private final PersonRepository  personRepository;
    private final ObjectMapper objectMapper;
    private final SendCredentials sendCredentials;




    //Display admin stats
    @Transactional(readOnly = true)
    public AdminStats displayAdminStats() {
        AdminStats stats = userRepository.getAdminStats();

        return stats != null ? stats : new AdminStats(0L, 0L, 0L, 0L);
    }

    @Transactional(readOnly = true)
    public boolean isEmailTaken(String email) {
        if (email == null || email.isBlank()) return false;
        return userRepository.existsBySystemEmail(email.trim());
    }

    @Transactional
    public void createAdminAccount(CreateAdmin request, User actor, String ipAddress) {
        Person person = personRepository.findById(request.personId())
                .orElseThrow(() -> new RuntimeException("Resource Not Found: Person record not found."));

        if (userManagementRepository.existsByPerson(person)) {
            throw new RuntimeException("Conflict: This person already has a linked system account.");
        }

        if (userManagementRepository.existsBySystemEmail(request.systemEmail())) {
            throw new RuntimeException("Conflict: System email already exists.");
        }

        String rawPassword = PasswordGenerator.generateRandomPassword(12);
        String encodedPassword = passwordEncoder.encode(rawPassword);

        Role adminRole = roleRepository.findByRoleName("ADMIN")
                .orElseThrow(() -> new RuntimeException("Resource Not Found: ADMIN role not found."));

        User user = new User();
        user.setPerson(person);
        user.setSystemEmail(request.systemEmail());
        user.setPassword(encodedPassword);
        user.setRole(adminRole);
        user.setStatus(request.activateImmediately() ? Status.ACTIVE : Status.INACTIVE);
        user.setNewAccount(true);

        if (request.departmentIds() != null && !request.departmentIds().isEmpty()) {
            Set<Department> depts = new HashSet<>(departmentRepository.findAllById(request.departmentIds()));
            user.setAllowedDepartments(depts);
        }

        if (request.permissionsIds() != null && !request.permissionsIds().isEmpty()) {
            Set<Permission> perms = new HashSet<>(permissionRepository.findAllById(request.permissionsIds()));
            user.setCustomPermissions(perms);
        }

        User savedUser = userManagementRepository.save(user);
        logAdminCreationAudit(savedUser, actor, ipAddress);

        String fullName = person.getFirstName() + " " + person.getLastName();
        sendCredentials.sendCredentials(request.systemEmail(), fullName, rawPassword);
    }

    private void logAdminCreationAudit(User newUser, User actor, String ip) {
        try {
            Map<String, Object> snapshot = new LinkedHashMap<>();
            snapshot.put("newUserId", newUser.getId());
            snapshot.put("systemEmail", newUser.getSystemEmail());
            snapshot.put("fullName", newUser.getPerson().getFirstName() + " " + newUser.getPerson().getLastName());
            snapshot.put("role", newUser.getRole().getRoleName());
            snapshot.put("assignedDepartments", newUser.getAllowedDepartments().stream().map(Department::getName).toList());
            snapshot.put("isNewAccount", newUser.isNewAccount());

            String jsonState = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(snapshot);

            auditLogService.log(
                    actor,
                    Departments.SYSTEM_ADMIN,
                    "Admin Management",
                    Severity.INFO,
                    "Register new admin account — " + newUser.getPerson().getFirstName() + " " + newUser.getPerson().getLastName(),
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
    public Page<AdminTable> displayAllAdminTables(String search, Status status, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("created_at").descending());

        Page<User> users = userRepository.findAllAdminsWithFilters(
                (search != null && !search.isBlank()) ? search.trim() : null,
                status != null ? status.name() : null,
                pageable
        );

        return users.map(user -> {
            var person = user.getPerson();

            return new AdminTable(
                    user.getId(),
                    person != null ? person.getPhoto() : null,
                    user.getAllowedDepartments().stream()
                            .map(Department::getName)
                            .collect(Collectors.toSet()),
                    user.getUsername(),
                    person != null ? person.getFirstName() : "N/A",
                    person != null ? person.getLastName() : "N/A",
                    user.getRole() != null ? user.getRole().getRoleName() : "N/A",
                    user.getCustomPermissions().stream()
                            .map(Permission::getPermissionName)
                            .collect(Collectors.toSet()),
                    user.getStatus() != null ? user.getStatus().name() : "UNKNOWN",
                    user.getLastLoginAt(),
                    user.getSystemEmail(),
                    user.getPerson().getAge(),
                    person != null ? person.getContactNumber() : "N/A",
                    person != null ? person.getGender() : "N/A",
                    person != null ? person.getCompleteAddress() : "N/A",
                    Boolean.TRUE.equals(user.getIsLocked()),
                    user.getCreatedAt(),
                    user.getLockUntil(),
                    user.getUpdatedAt()
            );
        });
    }

    @Transactional
    public void updateAdminAccount(UUID userId, UpdateAdmin request, User actor, String ipAddress) {
        User userToEdit = userManagementRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Resource Not Found: User not found."));

        Person person = userToEdit.getPerson();

        Map<String, Object> oldState = new LinkedHashMap<>();
        oldState.put("systemEmail", userToEdit.getSystemEmail());
        oldState.put("username", userToEdit.getUsername());
        oldState.put("departments", userToEdit.getAllowedDepartments().stream().map(Department::getName).sorted().toList());
        oldState.put("permissions", userToEdit.getCustomPermissions().stream().map(Permission::getPermissionName).sorted().toList());

        if (request.systemEmail() != null && !request.systemEmail().equals(userToEdit.getSystemEmail())) {
            if (userManagementRepository.existsBySystemEmail(request.systemEmail())) {
                throw new RuntimeException("Conflict: System email already exists.");
            }
            userToEdit.setSystemEmail(request.systemEmail());
            person.setEmail(request.systemEmail());
        }

        if (request.username() != null && !request.username().equals(userToEdit.getUsername())) {
            if (userManagementRepository.existsByUsername(request.username())) {
                throw new RuntimeException("Conflict: Username already exists.");
            }
            userToEdit.setUsername(request.username());
        }

        if (request.allDepartments()) {
            userToEdit.setAllowedDepartments(new HashSet<>(departmentRepository.findAll()));
        } else if (request.departmentIds() != null && !request.departmentIds().isEmpty()) {
            userToEdit.setAllowedDepartments(new HashSet<>(departmentRepository.findAllById(request.departmentIds())));
        }

        if (request.permissionIds() != null) {
            userToEdit.setCustomPermissions(new HashSet<>(permissionRepository.findAllById(request.permissionIds())));
        }

        User savedUser = userManagementRepository.save(userToEdit);

        Map<String, Object> newState = new LinkedHashMap<>();
        newState.put("systemEmail", savedUser.getSystemEmail());
        newState.put("username", savedUser.getUsername());
        newState.put("departments", savedUser.getAllowedDepartments().stream().map(Department::getName).sorted().toList());
        newState.put("permissions", savedUser.getCustomPermissions().stream().map(Permission::getPermissionName).sorted().toList());

        Map<String, Object> changesOld = new LinkedHashMap<>();
        Map<String, Object> changesNew = new LinkedHashMap<>();

        oldState.forEach((key, oldVal) -> {
            Object newVal = newState.get(key);
            if (!Objects.equals(oldVal, newVal)) {
                changesOld.put(key, oldVal);
                changesNew.put(key, newVal);
            }
        });

        if (!changesNew.isEmpty()) {
            try {
                auditLogService.log(
                        actor,
                        Departments.SYSTEM_ADMIN,
                        "Admin Management",
                        Severity.WARNING,
                        "Updated admin account  " + person.getFirstName() + " " + person.getLastName(),
                        ipAddress,
                        objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(changesOld),
                        objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(changesNew),
                        null
                );
            } catch (Exception e) {
                System.err.println("Audit Log Failure: " + e.getMessage());
            }
        }
    }



    @Transactional
    public void toggleUserLock(UUID userId, boolean lock, User actor, String ip, UserActionRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found."));

        user.setIsLocked(lock);

        if (lock) {
            user.setStatus(Status.LOCKED);
            user.setLockUntil(request.lockUntil());
        } else {
            user.setStatus(Status.ACTIVE);
            user.setFailedAttempts(0);
            user.setLockUntil(null);
        }

        userRepository.save(user);




        String oldStatus = lock ? "UNLOCKED" : "LOCKED";
        String newStatus = lock ? "LOCKED" : "UNLOCKED";

        String logMessage = String.format("Account for user %s has been %s.",
                user.getUsername(),
                lock ? "LOCKED" : "UNLOCKED");


        auditLogService.log(
                actor,
                Departments.SYSTEM_ADMIN,
                "USER_SECURITY",
                lock ? Severity.LOW : Severity.INFO,
                lock ? "LOCK_ACCOUNT" : "UNLOCK_ACCOUNT",
                ip,
                logMessage,
                oldStatus,
                newStatus
        );
    }




    @Transactional
    public void archiveAdminAccount(UUID userId, ArchiveAdmin request, User actor, String ipAddress) {
        User userToArchive = userManagementRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Resource Not Found: User not found."));

        if (userToArchive.getStatus() == Status.ARCHIVED) {
            throw new RuntimeException("Conflict: User is already archived.");
        }

        userToArchive.setStatus(Status.ARCHIVED);
        userToArchive.setStatusRemarks(request.remarks());
        userManagementRepository.save(userToArchive);

        Person person = userToArchive.getPerson();

        try {
            auditLogService.log(
                    actor,
                    Departments.SYSTEM_ADMIN,
                    "Admin Management",
                    Severity.WARNING,
                    "Archived admin account — " + person.getFirstName() + " " + person.getLastName(),
                    ipAddress,
                    null,
                    null,
                    null
            );
        } catch (Exception e) {
            System.err.println("Audit Log Failure: " + e.getMessage());
        }
    }

    @Transactional
    public void unarchiveAdminAccount(UUID userId, ArchiveAdmin restore, User actor, String ipAddress) {
        User userToRestore = userManagementRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Resource Not Found: User not found."));

        if (userToRestore.getStatus() == Status.ACTIVE) {
            throw new RuntimeException("Conflict: User is already active.");
        }

            userToRestore.setStatus(Status.ACTIVE);
        userToRestore.setStatusRemarks(restore.remarks());
        userManagementRepository.save(userToRestore);

        Person person = userToRestore.getPerson();

        try {
            auditLogService.log(
                    actor,
                    Departments.SYSTEM_ADMIN,
                    "Admin Management",
                    Severity.INFO,
                    "Restored admin account — " + person.getFirstName() + " " + person.getLastName(),
                    ipAddress,
                    null,
                    null,
                    null
            );
        } catch (Exception e) {
            System.err.println("Audit Log Failure: " + e.getMessage());
        }
    }




    @Transactional
    public void updateUserStatus(UUID userId, Status newStatus, User actor, String ip, String reason) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found."));

        Status oldStatus = user.getStatus();

        user.setStatus(newStatus);
        userRepository.save(user);


        Severity severity = (newStatus == Status.INACTIVE) ? Severity.WARNING : Severity.INFO;

        String logMessage = String.format(" Status updated for user '%s' from %s to %s.",
                 actor.getUsername(), oldStatus, newStatus);

        auditLogService.log(
                actor,
                Departments.SYSTEM_ADMIN,
                "Admin Management",
                severity,
                logMessage,
                ip,
                reason,
                oldStatus,
                newStatus);
    }




    @Transactional(readOnly = true)
    public UserSettingsPreview getMySettings(User currentUser) {
        return new UserSettingsPreview(
                currentUser.getId(),
                currentUser.getUsername(),
                currentUser.getSystemEmail(),
                currentUser.getPerson().getFirstName(),
                currentUser.getPerson().getLastName(),
                currentUser.getPerson().getContactNumber()
        );
    }


    @Transactional
    public void updateMySettings(User user, UserSettings dto, String ipAddress) {
        User managedUser = userRepository.findById(user.getId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        Person person = managedUser.getPerson();

        Map<String, Object> oldData = new LinkedHashMap<>();
        oldData.put("firstName", person.getFirstName());
        oldData.put("lastName", person.getLastName());
        oldData.put("email", managedUser.getSystemEmail());
        oldData.put("contactNumber", person.getContactNumber());

        person.setFirstName(dto.firstName());
        person.setLastName(dto.lastName());
        person.setContactNumber(dto.contactNumber());
        person.setEmail(dto.email());

        managedUser.setSystemEmail(dto.email());

        if (dto.password() != null && !dto.password().isBlank()) {
            managedUser.setPassword(passwordEncoder.encode(dto.password()));
        }

        userRepository.save(managedUser);

        Map<String, Object> newData = new LinkedHashMap<>();
        newData.put("firstName", dto.firstName());
        newData.put("lastName", dto.lastName());
        newData.put("email", dto.email());
        newData.put("contactNumber", dto.contactNumber());

        auditLogService.log(
                managedUser,
                managedUser.getRole().getRoleName().equalsIgnoreCase("ROOT_ADMIN")
                        ? Departments.ROOT_ADMIN : Departments.ADMINISTRATION,
                "USER_SETTINGS",
                Severity.INFO,
                "UPDATE_SELF_PROFILE",
                ipAddress,
                String.format("User %s (%s %s) updated their own profile details.",
                        managedUser.getUsername(), person.getFirstName(), person.getLastName()),
                oldData,
                newData
        );
    }


}
