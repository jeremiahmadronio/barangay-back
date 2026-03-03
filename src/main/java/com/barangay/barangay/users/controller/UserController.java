package com.barangay.barangay.users.controller;

import com.barangay.barangay.audit.service.IpAdressUtils;
import com.barangay.barangay.users.dto.AdminStats;
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

    //Creating admin account endpoint
    @PostMapping("/create-admin")
    public ResponseEntity<String> createAdmin(
            @Valid
            @RequestBody CreateAdmin createAdmin,
            @RequestParam UUID actorId,
            HttpServletRequest request
    ) {
        //checking if the user is root admin
        User user = userRepository.findById(actorId)
                .orElseThrow(() -> new RuntimeException("Actor not found."));

        if (!user.getRole().getRoleName().equals("ROOT_ADMIN")) {
            return ResponseEntity.status(403).body("Access Denied: Only Root Admin can create admin accounts.");
        }
         String ipAddress = IpAdressUtils.getClientIp(request);
        userService.createAdminAccount(createAdmin, user, ipAddress);

        return ResponseEntity.ok("Admin created successfully!");
    }




    @GetMapping("/stats")
    public ResponseEntity<AdminStats> displayAdminStats(
            @RequestParam UUID actorId
    ){
        //check if user is root admin.
        User user = userRepository.findById(actorId).
                orElseThrow(() -> new RuntimeException("user not found."));

        if(!user.getRole().getRoleName().equals("ROOT_ADMIN")){
            throw new RuntimeException("Only root admin can access.");
        }

        return ResponseEntity.ok(userService.displayAdminStats());
    }



}
