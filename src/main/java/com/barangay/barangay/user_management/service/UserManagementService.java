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
    public void createStaff(CreateUser dto, User actor, String ipAddress) {

        User managedActor = userManagementRepository.findById(actor.getId())
                .orElseThrow(() -> new RuntimeException("Actor admin not found"));

        Role role = roleRepository.findById(dto.roleId())
                .orElseThrow(() -> new RuntimeException("Role not found"));

        Set<Department> departments = new HashSet<>(
                departmentRepository.findAllById(dto.departmentIds())
        );

        Set<Permission> permissions = new HashSet<>(
                permissionRepository.findAllById(dto.permissionIds())
        );

        Set<Long> actorDepartmentIds = managedActor.getAllowedDepartments()
                .stream()
                .map(Department::getId)
                .collect(Collectors.toSet());

        if (!actorDepartmentIds.containsAll(dto.departmentIds())) {
            throw new RuntimeException("Access denied: You cannot assign departments you don't manage.");
        }

        if (departments.size() != dto.departmentIds().size()) {
            throw new RuntimeException("One or more department IDs are invalid.");
        }

        if (permissions.size() != dto.permissionIds().size()) {
            throw new RuntimeException("One or more permission IDs are invalid.");
        }

        Person person = new Person();
        person.setFirstName(dto.firstName());
        person.setLastName(dto.lastName());
        person.setEmail(dto.email());
        person.setContactNumber(dto.contactNumber());
        person = personRepository.save(person);


        User newUser = new User();
        newUser.setUsername(dto.username());
        newUser.setPerson(person);
        newUser.setSystemEmail(dto.email());
        newUser.setRole(role);
        newUser.setAllowedDepartments(departments);


        newUser.setCustomPermissions(permissions);


        newUser.setStatus(dto.activateImmediately() ? Status.ACTIVE : Status.INACTIVE);
        newUser.setPassword(passwordEncoder.encode(dto.password()));

        userManagementRepository.save(newUser);

        Map<String, Object> logValue = new LinkedHashMap<>();
        logValue.put("username", dto.username());
        logValue.put("fullName", dto.firstName() + " " + dto.lastName());
        logValue.put("email", dto.email());
        logValue.put("role", role.getRoleName());

        logValue.put("departments", departments.stream().map(Department::getName).toList());
        logValue.put("permissions", permissions.stream().map(Permission::getPermissionName).toList());

        auditLogService.log(
                managedActor,
                Departments.ADMINISTRATION,
                "USER_MANAGEMENT",
                Severity.WARNING,
                "CREATE_USER",
                ipAddress,
                "Created staff account for " + dto.username(),
                null,
                logValue
        );
    }


    @Transactional(readOnly = true)
    public Page<UserTable> getStaffTable(User currentAdmin, Pageable pageable,
                                         String search, String roleName, String departmentName) {

        // 1. Fetch the actual admin from DB to ensure associations are loaded
        User managedAdmin = userManagementRepository.findById(currentAdmin.getId())
                .orElseThrow(() -> new RuntimeException("Admin not found"));

        // 2. Filter by allowed departments
        Set<Long> deptIds = managedAdmin.getAllowedDepartments().stream()
                .map(Department::getId)
                .collect(Collectors.toSet());

        if (deptIds.isEmpty()) return Page.empty(pageable);

        // 3. Prepare parameters for the ILIKE query
        String searchParam = (search != null && !search.isBlank()) ? "%" + search.trim() + "%" : null;
        String roleParam   = (roleName != null && !roleName.isBlank()) ? roleName.trim() : null;
        String deptParam   = (departmentName != null && !departmentName.isBlank()) ? departmentName.trim() : null;

        // 4. Roles that should NOT be visible to regular admins
        List<String> excludedRoles = List.of("ROOT_ADMIN", "ADMIN");

        // 5. Execute search
        Page<User> users = userManagementRepository.findStaffByFilters(
                deptIds,
                currentAdmin.getId(),
                excludedRoles,
                searchParam,
                roleParam,
                deptParam,
                pageable
        );

        // 6. Map to DTO
        return users.map(user -> new UserTable(
                user.getId(),
                user.getUsername(),
                user.getPerson().getFirstName(),
                user.getPerson().getLastName(),
                user.getSystemEmail(),
                user.getPerson().getContactNumber(),
                user.getRole() != null ? user.getRole().getRoleName() : "N/A",
                user.getAllowedDepartments().stream()
                        .map(Department::getName)
                        .collect(Collectors.joining(", ")),
                user.getCustomPermissions() != null ? user.getCustomPermissions().stream()
                        .map(Permission::getPermissionName)
                        .collect(Collectors.toSet()) : Collections.emptySet(),
                Boolean.TRUE.equals(user.getIsLocked()),
                user.getStatus().name(),
                user.getCreatedAt(),
                user.getLastLoginAt(),
                user.getLockUntil(),
                user.getUpdatedAt()
        ));
    }





    @Transactional
    public void updateStaff(UUID userId, EditUser dto, User actor, String ipAddress) {
        User user = userManagementRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Staff user not found"));

        Person person = user.getPerson();

        String oldState = formatUserDetailsForAudit(user);

        if (dto.firstName() != null) person.setFirstName(dto.firstName());
        if (dto.lastName() != null) person.setLastName(dto.lastName());
        if (dto.email() != null) user.setSystemEmail(dto.email());
        if (dto.contactNumber() != null) person.setContactNumber(dto.contactNumber());

        if (dto.roleId() != null) {
            Role role = roleRepository.findById(dto.roleId())
                    .orElseThrow(() -> new RuntimeException("Role not found"));
            user.setRole(role);
        }

        if (dto.departmentIds() != null && !dto.departmentIds().isEmpty()) {
            Set<Department> depts = new HashSet<>(departmentRepository.findAllById(dto.departmentIds()));
            user.setAllowedDepartments(depts);
        }

        if (dto.permissionIds() != null && !dto.permissionIds().isEmpty()) {
            Set<Permission> perms = new HashSet<>(permissionRepository.findAllById(dto.permissionIds()));
            user.setCustomPermissions(perms);
        }

        if (dto.password() != null && !dto.password().isBlank()) {
            user.setPassword(passwordEncoder.encode(dto.password()));
        }

        User updatedUser = userManagementRepository.save(user);

        String newState = formatUserDetailsForAudit(updatedUser);

        auditLogService.log(
                actor,
                Departments.ADMINISTRATION,
                "USER_MANAGEMENT",
                Severity.INFO,
                "UPDATE_USER",
                ipAddress,
                "Updated account details for " + user.getUsername(),
                oldState,
                newState
        );
    }

    private String formatUserDetailsForAudit(User user) {
        String depts = user.getAllowedDepartments().stream()
                .map(Department::getName)
                .collect(Collectors.joining(", "));

        String perms = user.getCustomPermissions().stream()
                .map(Permission::getPermissionName)
                .collect(Collectors.joining(", "));

        return String.format(
                "Name: %s %s | Email: %s | Role: %s | Departments: [%s] | Custom Permissions: [%s]",
                user.getPerson().getFirstName(),
                user.getPerson().getLastName(),
                user.getSystemEmail(),
                user.getRole() != null ? user.getRole().getRoleName() : "N/A",
                depts.isEmpty() ? "None" : depts,
                perms.isEmpty() ? "None" : perms
        );
    }
}
