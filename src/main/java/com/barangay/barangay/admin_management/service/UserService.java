package com.barangay.barangay.admin_management.service;

import com.barangay.barangay.audit.service.AuditLogService;
import com.barangay.barangay.department.model.Department;
import com.barangay.barangay.role.model.Role;
import com.barangay.barangay.department.repository.DepartmentRepository;
import com.barangay.barangay.role.repository.RoleRepository;
import com.barangay.barangay.enumerated.Departments;
import com.barangay.barangay.enumerated.Severity;
import com.barangay.barangay.enumerated.Status;
import com.barangay.barangay.admin_management.dto.*;
import com.barangay.barangay.admin_management.model.User;
import com.barangay.barangay.admin_management.repository.Root_AdminRepository;
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
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuditLogService auditLogService;




    //Display admin stats
    @Transactional(readOnly = true)
    public AdminStats displayAdminStats() {
        AdminStats stats = userRepository.getAdminStats();

        return stats != null ? stats : new AdminStats(0L, 0L, 0L, 0L);
    }


    @Transactional
    public void createAdminAccount(CreateAdmin createAdmin, User actor, String ipAddress) {

        if (userRepository.existsByEmail(createAdmin.email())) {
            throw new RuntimeException("Email already exists.");
        }

        if (userRepository.existsByUsername(createAdmin.username())) {
            throw new RuntimeException("Username already exists.");
        }

        Role selectedRole = roleRepository.findById(createAdmin.roleId())
                .orElseThrow(() -> new RuntimeException("Selected role not found."));

        Set<Department> departments = new HashSet<>();
        if (createAdmin.allDepartments()) {
            departments.addAll(departmentRepository.findAll());
        } else {
            departments.addAll(departmentRepository.findAllById(createAdmin.departmentIds()));
        }

        User newAdmin = new User();
        newAdmin.setFirstName(createAdmin.firstName());
        newAdmin.setLastName(createAdmin.lastName());
        newAdmin.setEmail(createAdmin.email());
        newAdmin.setUsername(createAdmin.username());
        newAdmin.setPassword(passwordEncoder.encode(createAdmin.password()));
        newAdmin.setContactNumber(createAdmin.contactNumber());
        newAdmin.setRole(selectedRole);
        newAdmin.setAllowedDepartments(departments);
        newAdmin.setStatus(createAdmin.activateImmediately() ? Status.ACTIVE : Status.INACTIVE);
        newAdmin.setFailedAttempts(0);
        newAdmin.setIsLocked(false);

        User savedAdmin = userRepository.save(newAdmin);

        String departmentNames = departments.stream()
                .map(Department::getName)
                .collect(Collectors.joining(", "));

        String newState = String.format(
                "Username: %s | Full Name: %s %s | Role: %s | Departments: [%s]",
                savedAdmin.getUsername(),
                savedAdmin.getFirstName(),
                savedAdmin.getLastName(),
                selectedRole.getRoleName(),
                departmentNames
        );

        auditLogService.log(
                actor,
                Departments.ADMINISTRATION,
                "USER_MANAGEMENT",
                Severity.WARNING,
                "CREATE_ADMIN",
                ipAddress,
                "Root Admin created a new Admin account for " + savedAdmin.getFirstName() + " " + savedAdmin.getLastName(),
                null,
                newState
        );
    }


    @Transactional(readOnly = true)
    public Page<AdminTable> displayAllAdminTables(String search, Status status, int page, int size) {

        // 1. Setup Pagination & Sorting
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());

        // 2. Query the Repository (Search at Status na lang ang pinapasa natin) [cite: 2026-03-09]
        Page<User> users = userRepository.findAllAdminsWithFilters(
                (search != null && !search.isBlank()) ? search.trim() : null,
                status,
                pageable
        );

        // 3. Map to AdminTable DTO (Null-safe approach) [cite: 2026-03-09]
        return users.map(user -> new AdminTable(
                user.getId(),
                user.getUsername(),
                user.getFirstName(),
                user.getLastName(),
                user.getEmail(),
                user.getContactNumber(),
                user.getRole() != null ? user.getRole().getRoleName() : "N/A",
                user.getAllowedDepartments().stream()
                        .map(Department::getName)
                        .collect(Collectors.toSet()),
                Boolean.TRUE.equals(user.getIsLocked()),
                user.getStatus() != null ? user.getStatus().name() : "UNKNOWN",
                user.getCreatedAt(),
                user.getLastLoginAt(),
                user.getLockUntil(),
                user.getUpdatedAt()
        ));
    }


    @Transactional
    public void updateAdminAccount(UUID userId, UpdateAdmin updateDto, User actor, String ipAddress) {
        User userToEdit = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User to edit not found."));

        AdminTable oldState = mapToAdminTable(userToEdit);

        if (userRepository.existsByEmailAndIdNot(updateDto.email(), userId)) {
            throw new RuntimeException("Email is already taken.");
        }
        if (userRepository.existsByUsernameAndIdNot(updateDto.username(), userId)) {
            throw new RuntimeException("Username is already taken.");
        }

        // 3. I-apply ang Updates
        userToEdit.setFirstName(updateDto.firstName());
        userToEdit.setLastName(updateDto.lastName());
        userToEdit.setEmail(updateDto.email());
        userToEdit.setUsername(updateDto.username());
        userToEdit.setContactNumber(updateDto.contactNumber());

        if (updateDto.password() != null && !updateDto.password().isBlank()) {
            userToEdit.setPassword(passwordEncoder.encode(updateDto.password()));
        }

        Set<Department> newDepts = new HashSet<>();
        if (updateDto.allDepartments()) {
            newDepts.addAll(departmentRepository.findAll());
        } else {
            newDepts.addAll(departmentRepository.findAllById(updateDto.departmentIds()));
        }
        userToEdit.setAllowedDepartments(newDepts);

        User savedUser = userRepository.save(userToEdit);
        AdminTable newState = mapToAdminTable(savedUser);

        Map<String, Object> changesOld = new HashMap<>();
        Map<String, Object> changesNew = new HashMap<>();

        compareAndAdd(changesOld, changesNew, "First Name", oldState.firstName(), newState.firstName());
        compareAndAdd(changesOld, changesNew, "Last Name", oldState.lastName(), newState.lastName());
        compareAndAdd(changesOld, changesNew, "Email", oldState.email(), newState.email());
        compareAndAdd(changesOld, changesNew, "Username", oldState.username(), newState.username());
        compareAndAdd(changesOld, changesNew, "Contact", oldState.contactNumber(), newState.contactNumber());
        compareAndAdd(changesOld, changesNew, "Departments", oldState.departments(), newState.departments());

        if (!changesNew.isEmpty()) {
            auditLogService.log(
                    actor,
                    Departments.ROOT_ADMIN,
                    "USER_MANAGEMENT",
                    Severity.WARNING,
                    "UPDATE_ADMIN",
                    ipAddress,
                    "Updated admin account for: " + savedUser.getFirstName() + " " + savedUser.getLastName(),
                    changesOld,
                    changesNew
            );
        }
    }

    private void compareAndAdd(Map<String, Object> oldMap, Map<String, Object> newMap, String field, Object oldVal, Object newVal) {
        if (oldVal == null && newVal == null) return;
        if (oldVal != null && oldVal.equals(newVal)) return;

        oldMap.put(field, oldVal);
        newMap.put(field, newVal);
    }

    private AdminTable mapToAdminTable(User user) {
        return new AdminTable(
                user.getId(),
                user.getUsername(),
                user.getFirstName(),
                user.getLastName(),
                user.getEmail(),
                user.getContactNumber(),
                user.getRole().getRoleName(),
                user.getAllowedDepartments().stream()
                        .map(Department::getName)
                        .sorted()
                        .collect(Collectors.toSet()),
                user.getIsLocked(),
                user.getStatus().name(),
                user.getCreatedAt(),
                user.getLastLoginAt(),
                user.getLockUntil(),
                user.getUpdatedAt()
        );
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

        String lockDuration = request.lockUntil() != null ? request.lockUntil().toString() : "Manual";



        String oldStatus = lock ? "UNLOCKED" : "LOCKED";
        String newStatus = lock ? "LOCKED" : "UNLOCKED";

        String logMessage = String.format("Account for user %s has been %s.",
                user.getUsername(),
                lock ? "LOCKED" : "UNLOCKED");


        auditLogService.log(
                actor,
                Departments.ROOT_ADMIN,
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
    public void updateUserStatus(UUID userId, Status newStatus, User actor, String ip, String reason) {
        //check user if exist
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
                Departments.ROOT_ADMIN,
                "USER_MANAGEMENT",
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
                currentUser.getEmail(),
                currentUser.getFirstName(),
                currentUser.getLastName(),
                currentUser.getContactNumber()
        );
    }


    @Transactional
    public void updateMySettings(User user, UserSettings dto, String ipAddress) {
        User managedUser = userRepository.findById(user.getId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        Map<String, Object> oldData = new LinkedHashMap<>();
        oldData.put("firstName", managedUser.getFirstName());
        oldData.put("lastName", managedUser.getLastName());
        oldData.put("email", managedUser.getEmail());
        oldData.put("contactNumber", managedUser.getContactNumber());

        managedUser.setFirstName(dto.firstName());
        managedUser.setLastName(dto.lastName());
        managedUser.setContactNumber(dto.contactNumber());

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
                String.format("User %s updated their own profile details.", managedUser.getUsername()),
                oldData,
                newData
        );
    }


}
