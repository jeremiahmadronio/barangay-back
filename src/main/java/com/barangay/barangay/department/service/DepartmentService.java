package com.barangay.barangay.department.service;

import com.barangay.barangay.department.dto.DepartmentOptions;
import com.barangay.barangay.department.repository.DepartmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DepartmentService {

    private final DepartmentRepository departmentRepository;

    @Transactional(readOnly = true)
    public List<DepartmentOptions> getAllDepartmentOptions() {
        return departmentRepository.findAll().stream()

                .filter(dept -> !dept.getName().equalsIgnoreCase("ROOT_ADMIN"))

                .map(dept -> new DepartmentOptions(dept.getId(), dept.getName()))
                .collect(Collectors.toList());
    }
}