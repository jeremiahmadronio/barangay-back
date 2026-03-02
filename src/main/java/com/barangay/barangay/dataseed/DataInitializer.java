package com.barangay.barangay.dataseed;

import com.barangay.barangay.auth.model.Department;
import com.barangay.barangay.auth.model.Permission;
import com.barangay.barangay.auth.model.Role;
import com.barangay.barangay.users.model.User;
import com.barangay.barangay.auth.repository.DepartmentRepository;
import com.barangay.barangay.auth.repository.PermissionRepository;
import com.barangay.barangay.auth.repository.RoleRepository;
import com.barangay.barangay.users.repository.UserRepository;
import com.barangay.barangay.enumerated.Status;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final PermissionRepository permissionRepository;
    private final DepartmentRepository departmentRepository;

    @Override
    @Transactional
    public void run(String... args) {
        Department adminDept = createDeptIfNotFound("ADMINISTRATION");
        createDeptIfNotFound("VAWC");
        createDeptIfNotFound("BLOTTER");
        createDeptIfNotFound("KAPITANA");
        createDeptIfNotFound("BCPC");
        createDeptIfNotFound("CLEARANCE");
        createDeptIfNotFound("LUPONG TAGAPAMAYAPA");
        createDeptIfNotFound("OPERATIONAL STAFF");
        createDeptIfNotFound("FTJS");
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

        if (userRepository.findByUsername("rootadmin").isEmpty()) {
            User root = new User();
            root.setUsername("rootadmin");
            root.setPassword("admin123");
            root.setEmail("admin@ugong.gov.ph");
            root.setFirstName("Juan");
            root.setLastName("Dela Cruz");
            root.setStatus(Status.ACTIVE);
            root.setRole(rootRole);
            root.setAllowedDepartments(new HashSet<>(Set.of(adminDept)));
            root.setFailedAttempts(0);
            root.setIsLocked(false);
            userRepository.save(root);
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