package com.barangay.barangay.role.service;

import com.barangay.barangay.role.dto.RoleOptions;
import com.barangay.barangay.role.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RoleService {

    private final RoleRepository roleRepository;

    @Transactional(readOnly = true)
    public List<RoleOptions> getAdminRoleOptions() {
        return roleRepository.findAll().stream()
                .filter(role -> role.getRoleName().equalsIgnoreCase("ADMIN"))
                .map(role -> new RoleOptions(role.getId(), role.getRoleName()))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<RoleOptions> getStaffRoleOptions() {
        List<String> excludedRoles = List.of("ROOT_ADMIN", "ADMIN");

        return roleRepository.findAll().stream()
                .filter(role -> !excludedRoles.contains(role.getRoleName().toUpperCase()))
                .map(role -> new RoleOptions(role.getId(), role.getRoleName()))
                .collect(Collectors.toList());
    }
}