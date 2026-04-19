package com.barangay.barangay.department.service;

import com.barangay.barangay.admin_management.model.User;
import com.barangay.barangay.department.dto.DepartmentOptions;
import com.barangay.barangay.department.repository.DepartmentRepository;
import com.barangay.barangay.department.repository.UserDepartmentRepository;
import com.barangay.barangay.user_management.repository.UserManagementRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
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
                .map(dept -> new DepartmentOptions(
                        dept.getId(),
                        formatDepartmentName(dept.getName())
                ))
                .sorted(Comparator.comparing(DepartmentOptions::name))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<DepartmentOptions> getAssignedDepartmentOptions() {
        String currentUsername = SecurityContextHolder.getContext().getAuthentication().getName();

        User user = userDepartmentRepository.findByEmailWithDepartments(currentUsername)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return user.getAllowedDepartments().stream()
                .map(dept -> new DepartmentOptions(
                        dept.getId(),
                        formatDepartmentName(dept.getName())
                ))
                .sorted(Comparator.comparing(DepartmentOptions::name))
                .collect(Collectors.toList());
    }

    private String formatDepartmentName(String rawName) {
        if (rawName == null || rawName.isBlank()) return "Unknown Department";

        return switch (rawName.toUpperCase()) {
            case "VAWC" -> "VAWC (Violence Against Women and Children)";
            case "BCPC" -> "BCPC (Council for the Protection of Children)";
            case "FTJS" -> "FTJS (First Time Job Seekers)";
            case "LUPONG_TAGAPAMAYAPA" -> "Lupong Tagapamayapa";
            case "CLEARANCE" -> "Barangay Clearance & Certification";
            case "BLOTTER" -> "Blotter Management";
            case "KAPITANA" -> "Office of the Barangay Captain";
            case "ADMINISTRATION" -> "Administration";
            default -> {
                String text = rawName.replace("_", " ").toLowerCase();
                StringBuilder formatted = new StringBuilder();
                for (String word : text.split(" ")) {
                    if (!word.isEmpty()) {
                        formatted.append(Character.toUpperCase(word.charAt(0)))
                                .append(word.substring(1)).append(" ");
                    }
                }
                yield formatted.toString().trim();
            }
        };
    }
}