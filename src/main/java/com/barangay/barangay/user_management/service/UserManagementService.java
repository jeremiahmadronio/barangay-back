package com.barangay.barangay.user_management.service;

import com.barangay.barangay.audit.service.AuditLogService;
import com.barangay.barangay.department.repository.DepartmentRepository;
import com.barangay.barangay.enumerated.Departments;
import com.barangay.barangay.enumerated.Severity;
import com.barangay.barangay.enumerated.Status;
import com.barangay.barangay.role.model.Role;
import com.barangay.barangay.role.repository.RoleRepository;
import com.barangay.barangay.user_management.dto.CreateUser;
import com.barangay.barangay.user_management.dto.UserStats;
import com.barangay.barangay.user_management.repository.UserManagementRepository;
import com.barangay.barangay.department.model.Department;
import com.barangay.barangay.admin_management.model.User;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserManagementService {

    private final UserManagementRepository userManagementRepository;

    private final RoleRepository roleRepository;
    private final DepartmentRepository departmentRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuditLogService auditLogService;


    //stats
    @Transactional(readOnly = true)
    public UserStats getDashboardStats(User currentAdmin) {
        Set<Long> deptIds = currentAdmin.getAllowedDepartments().stream()
                .map(Department::getId)
                .collect(Collectors.toSet());

        if (deptIds.isEmpty()) {
            return new UserStats(currentAdmin.getId(), 0L, 0L, 0L, 0L);
        }

        return new UserStats(
                currentAdmin.getId(),
                userManagementRepository.countUsersByDepartments(deptIds),
                userManagementRepository.countActiveUsersByDepartments(deptIds),
                userManagementRepository.countInactiveUsersByDepartments(deptIds),
                userManagementRepository.countLockedUsersByDepartments(deptIds)
        );
    }



    @Transactional
    public void createAdmin(CreateUser dto, User actor, String ipAddress) {

        //  Validate Role
        Role role = roleRepository.findById(dto.roleId())
                .orElseThrow(() -> new RuntimeException("Role not found"));

        //  Get the actor's allowed department IDs
        Set<Long> actorDepartmentIds = actor.getAllowedDepartments()
                .stream()
                .map(Department::getId)
                .collect(Collectors.toSet());

        //  Check if ALL requested departments are within the actor's allowed departments
        //    If the actor doesn't own even ONE of the requested departments, throw an error
        boolean hasAccess = actorDepartmentIds.containsAll(dto.departmentIds());
        if (!hasAccess) {
            throw new RuntimeException(
                    "Access denied: You can only assign departments that you are authorized to manage."
            );
        }

        //  Fetch the Department entities using the validated IDs
        Set<Department> departments = new HashSet<>(
                departmentRepository.findAllById(dto.departmentIds())
        );

        //  Make sure all requested department IDs actually exist in the DB
        if (departments.size() != dto.departmentIds().size()) {
            throw new RuntimeException("One or more department IDs do not exist.");
        }

        User newUser = new User();
        newUser.setUsername(dto.username());
        newUser.setFirstName(dto.firstName());
        newUser.setLastName(dto.lastName());
        newUser.setEmail(dto.email());
        newUser.setContactNumber(dto.contactNumber());
        newUser.setRole(role);
        newUser.setAllowedDepartments(departments);
        newUser.setStatus(dto.activateImmediately() ? Status.ACTIVE : Status.INACTIVE);
        newUser.setPassword(passwordEncoder.encode(dto.password()));

        userManagementRepository.save(newUser);

        // 7. Audit log
        auditLogService.log(
                actor,
                Departments.ADMINISTRATION,
                "USER_MANAGEMENT",
                Severity.INFO,
                "CREATE_ADMIN",
                ipAddress,
                "Created new admin account for " + dto.firstName() + " " + dto.lastName(),
                null,
                dto
        );
    }


}
