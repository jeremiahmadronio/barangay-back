package com.barangay.barangay.users.service;

import com.barangay.barangay.audit.service.AuditLogService;
import com.barangay.barangay.auth.model.Department;
import com.barangay.barangay.auth.model.Role;
import com.barangay.barangay.auth.repository.DepartmentRepository;
import com.barangay.barangay.auth.repository.RoleRepository;
import com.barangay.barangay.enumerated.Severity;
import com.barangay.barangay.enumerated.Status;
import com.barangay.barangay.users.dto.*;
import com.barangay.barangay.users.model.User;
import com.barangay.barangay.users.repository.UserRepository;
import jakarta.persistence.Table;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {


    private final DepartmentRepository departmentRepository;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuditLogService auditLogService;




    //Display admin stats
    @Transactional(readOnly = true)
    public AdminStats displayAdminStats(){
        return userRepository.getAdminStats();
    }



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
                "ROOT_ADMIN",
                Severity.INFO,
                "CREATE_ADMIN",
                ipAddress,
                "Created admin account for: " + savedAdmin.getFirstName() + savedAdmin.getLastName() ,
                null,
                createAdmin
        );

    }



    @Transactional
    public Page<AdminTable> displayAllAdminTables(String search,String role, Status status, int page, int size){

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());

        Page<User> users = userRepository.findAllAdminsWithFilters(search , role,status, pageable);

        return users.map(user -> new AdminTable(
                user.getId(),
                user.getUsername(),
                user.getFirstName(),
                user.getLastName(),
                user.getEmail(),
                user.getContactNumber(),
                user.getRole().getRoleName(),
                user.getAllowedDepartments().stream().map(Department::getName).collect(Collectors.toSet()),
                user.getIsLocked(),
                user.getStatus().name(),
                user.getCreatedAt(),
                user.getLastLoginAt(),
                user.getLockUntil(),
                user.getUpdatedAt()
        ));
    }



   //update admin
    @Transactional
    public void updateAdminAccount(UUID userId, UpdateAdmin updateDto, User actor, String ipAddress) {

        //check if id passes is already existing
        User userToEdit = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User to edit not found."));

        //get all value for audit logs
        AdminTable oldState = new AdminTable(
                userToEdit.getId(),
                userToEdit.getUsername(),
                userToEdit.getFirstName(),
                userToEdit.getLastName(),
                userToEdit.getEmail(),
                userToEdit.getContactNumber(),
                userToEdit.getRole().getRoleName(),
                userToEdit.getAllowedDepartments().stream().map(Department::getName).collect(Collectors.toSet()),
                userToEdit.getIsLocked(),
                userToEdit.getStatus().name(),
                userToEdit.getCreatedAt(),
                userToEdit.getLastLoginAt(),
                userToEdit.getLockUntil(),
                userToEdit.getUpdatedAt()
        );

        //check if other user use this email
        if (userRepository.existsByEmailAndIdNot(updateDto.email(), userId)) {
            throw new RuntimeException("Email is already taken by another account.");
        }
        //check if other user use this username
        if (userRepository.existsByUsernameAndIdNot(updateDto.username(), userId)) {
            throw new RuntimeException("Username is already taken.");
        }

       //updating data from data to database
        userToEdit.setFirstName(updateDto.firstName());
        userToEdit.setLastName(updateDto.lastName());
        userToEdit.setEmail(updateDto.email());
        userToEdit.setUsername(updateDto.username());
        userToEdit.setContactNumber(updateDto.contactNumber());
        Role newRole = roleRepository.findById(updateDto.roleId())
                .orElseThrow(() -> new RuntimeException("Role not found."));
        userToEdit.setRole(newRole);
        Set<Department> departments = new HashSet<>();
        if (updateDto.allDepartments()) {
            departments.addAll(departmentRepository.findAll());
        } else {
            departments.addAll(departmentRepository.findAllById(updateDto.departmentIds()));
        }
        userToEdit.setAllowedDepartments(departments);

        //save update to database
        userRepository.save(userToEdit);

        //audit logs for changes
        auditLogService.log(
                actor,
                null,
                "USER_MANAGEMENT",
                Severity.INFO,
                "UPDATE_ADMIN",
                ipAddress,
                "Update admin account for: " + userToEdit.getFirstName() + userToEdit.getLastName() ,
                oldState,
                userToEdit
        );
    }


    @Transactional
    public void toggleUserLock(UUID userId, boolean lock, User actor, String ip, UserActionRequest request) {
        //check if user exist
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found."));

        //updating user status
        user.setIsLocked(lock);

        if (lock) {
            user.setStatus(Status.LOCKED);
            user.setLockUntil(request.lockUntil());
        } else {
            user.setFailedAttempts(0);
            user.setLockUntil(null);
        }

        //forward to repository and save to database
        userRepository.save(user);


        //message for audit logs
        String logMessage = String.format("%s account for %s. Reason: %s",
                lock ? "Locked until " + (request.lockUntil() != null ? request.lockUntil() : "Manual Unlock") : "Unlocked",
                user.getUsername(),
                request.reason()
        );

        //audit logs
        auditLogService.log(
                actor,
                null,
                "USER_SECURITY",
                lock ? Severity.WARN : Severity.INFO,
                lock ? "LOCK_ACCOUNT" : "UNLOCK_ACCOUNT",
                ip,
                logMessage,
                !lock,
                lock
        );
    }




    @Transactional
    public void updateUserStatus(UUID userId, Status newStatus, User actor, String ip, String reason) {
        //check user if exist
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found."));

        //get old status for audit logs
        Status oldStatus = user.getStatus();

        //update new status
        user.setStatus(newStatus);
        //forward to repository and save to database
        userRepository.save(user);


        Severity severity = (newStatus == Status.INACTIVE) ? Severity.WARN : Severity.INFO;
        auditLogService.log(
                actor,
                null,
                "USER_MANAGEMENT",
                severity,
                "STATUS_CHANGE",
                ip,
                "Changed status for " + user.getUsername() + " to " + newStatus + ". Reason: " + reason,
                oldStatus,
                newStatus);
    }





}
