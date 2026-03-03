package com.barangay.barangay.users.service;

import com.barangay.barangay.audit.service.AuditLogService;
import com.barangay.barangay.auth.model.Department;
import com.barangay.barangay.auth.model.Role;
import com.barangay.barangay.auth.repository.DepartmentRepository;
import com.barangay.barangay.auth.repository.RoleRepository;
import com.barangay.barangay.enumerated.Status;
import com.barangay.barangay.users.dto.AdminStats;
import com.barangay.barangay.users.dto.CreateAdmin;
import com.barangay.barangay.users.model.User;
import com.barangay.barangay.users.repository.UserRepository;
import jakarta.persistence.Table;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class UserService {


    private final DepartmentRepository departmentRepository;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuditLogService auditLogService;


    //Create Admin Account and implementing role based access and audit logs
    @Transactional
    public void createAdminAccount (CreateAdmin createAdmin,User actor , String ipAddress){

        // email must be unique
        if(userRepository.existsByEmail(createAdmin.email())){
            throw new RuntimeException("Email Already Exists");
        }
        // username must be unique
        if(userRepository.existsByUsername(createAdmin.username())){
            throw new RuntimeException("Username Already Exists");
        }

        // check if a role passed by the front end is on database
        Role selectedRole = roleRepository.findById(createAdmin.roleId())
                .orElseThrow(() -> new RuntimeException("Selected Role not found."));


        // set all departments if root admin choose all departments
        // and if not, please provide a list of ids in department that created admin can access
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

        //save to database
        User savedAdmin = userRepository.save(newAdmin);

        //audit logs
        auditLogService.log(
                actor,
                null,
                "USER_MANAGEMENT",
                "INFO",
                "CREATE_ADMIN",
                ipAddress,
                "Created admin account for: " + savedAdmin.getFirstName() + savedAdmin.getLastName() ,
                null,
                createAdmin
        );

    }


    //Display admin stats
    public AdminStats displayAdminStats(){
        return userRepository.getAdminStats();
    }





}
