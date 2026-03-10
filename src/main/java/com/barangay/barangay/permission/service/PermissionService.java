package com.barangay.barangay.permission.service;

import com.barangay.barangay.permission.dto.PermissionOptions;
import com.barangay.barangay.permission.repository.PermissionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PermissionService {

    private final PermissionRepository permissionRepository;

    @Transactional(readOnly = true)
    public List<PermissionOptions> getAllPermissionOptions() {
        return permissionRepository.findAll().stream()
                .map(permission -> new PermissionOptions(
                        permission.getId(),
                        permission.getPermissionName()
                ))
                .collect(Collectors.toList());
    }
}
