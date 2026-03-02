package com.barangay.barangay.users.controller;

import com.barangay.barangay.audit.service.IpAdressUtils;
import com.barangay.barangay.users.dto.CreateAdmin;
import com.barangay.barangay.users.model.User;
import com.barangay.barangay.users.repository.UserRepository;
import com.barangay.barangay.users.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Controller
@RestController
@RequestMapping("api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final UserRepository userRepository;

    @PostMapping("/create-admin")
    public ResponseEntity<String> createAdmin(
            @Valid
            @RequestBody CreateAdmin createAdmin,
            @RequestParam UUID actorId,
            HttpServletRequest request
    ) {
        User actor = userRepository.findById(actorId)
                .orElseThrow(() -> new RuntimeException("Actor not found."));

        String ipAddress = IpAdressUtils.getClientIp(request);
        userService.createAdminAccount(createAdmin, actor, ipAddress);

        return ResponseEntity.ok("Admin created successfully!");
    }
}
