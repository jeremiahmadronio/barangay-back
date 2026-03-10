package com.barangay.barangay.department.service;

import com.barangay.barangay.admin_management.model.User;
import com.barangay.barangay.department.dto.DepartmentOptions;
import com.barangay.barangay.department.repository.DepartmentRepository;
import com.barangay.barangay.department.repository.UserDepartmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DepartmentService {

    private final DepartmentRepository departmentRepository;
    private final UserDepartmentRepository userDepartmentRepository;

    @Transactional(readOnly = true)
    public List<DepartmentOptions> getAllDepartmentOptions() {
        return departmentRepository.findAll().stream()

                .filter(dept -> !dept.getName().equalsIgnoreCase("ROOT_ADMIN")
                        && !dept.getName().equalsIgnoreCase("ADMINISTRATION"))
                .map(dept -> new DepartmentOptions(dept.getId(), dept.getName()))
                .collect(Collectors.toList());
    }


    @Transactional(readOnly = true)
    public List<DepartmentOptions> getAssignedDepartmentOptions() {
        String currentUsername = SecurityContextHolder.getContext().getAuthentication().getName();

        User user = userDepartmentRepository.findByEmailWithDepartments(currentUsername)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return user.getAllowedDepartments().stream()
                .map(dept -> new DepartmentOptions(dept.getId(), dept.getName()))
                .collect(Collectors.toList());
    }
}