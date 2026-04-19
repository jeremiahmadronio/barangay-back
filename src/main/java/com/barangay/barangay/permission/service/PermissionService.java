package com.barangay.barangay.permission.service;

import com.barangay.barangay.admin_management.model.User;
import com.barangay.barangay.department.model.Department;
import com.barangay.barangay.department.repository.DepartmentRepository;
import com.barangay.barangay.permission.dto.PermissionOptions;
import com.barangay.barangay.permission.dto.UserAccessPermission;
import com.barangay.barangay.permission.model.Permission;
import com.barangay.barangay.permission.repository.PermissionRepository;
import com.barangay.barangay.permission.repository.UserAccessPermissionRepository;
import com.barangay.barangay.user_management.repository.UserManagementRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PermissionService {

    private final PermissionRepository permissionRepository;
    private final UserAccessPermissionRepository userAccessPermissionRepository;

     private final DepartmentRepository departmentRepository;

    @Transactional(readOnly = true)
    public List<PermissionOptions> getPermissionOptions() {


        return permissionRepository.findAll().stream()
                .map(permission -> new PermissionOptions(
                        permission.getId(),
                        permission.getPermissionName()
                ))
                .collect(Collectors.toList());
    }


    @Transactional(readOnly = true)
    public UserAccessPermission getMySecurityProfile(UUID userId) {
        User user = userAccessPermissionRepository.findUserWithSecurityDetails(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        String deptName = user.getAllowedDepartments().isEmpty() ? "NO_DEPARTMENT" :
                user.getAllowedDepartments().iterator().next().getName();

        List<String> allPermissions = user.getCustomPermissions().stream()
                .map(Permission::getPermissionName)
                .toList();

        return new UserAccessPermission(
                user.getId(),
                user.getUsername(),
                user.getRole().getRoleName(),
                deptName,
                allPermissions
        );
    }




}
