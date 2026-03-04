package com.barangay.barangay.auth.service;

import com.barangay.barangay.audit.service.AuditLogService;
import com.barangay.barangay.auth.dto.Login;
import com.barangay.barangay.auth.dto.LoginResponse;
import com.barangay.barangay.enumerated.Severity;
import com.barangay.barangay.security.CustomUserDetails;
import com.barangay.barangay.security.JwtService;
import com.barangay.barangay.users.model.User;
import com.barangay.barangay.users.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;

@Service
@RequiredArgsConstructor
public class AuthenticationService {

    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final AuditLogService auditLogService;




    //login
    public LoginResponse authenticate(Login request, String ipAddress) {

        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.email(),
                        request.password()
                )
        );

        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new RuntimeException("User not found"));

        user.setLastLoginAt(LocalDateTime.now());
        user.setFailedAttempts(0);
        userRepository.save(user);

        HashMap<String, Object> extraClaims = new HashMap<>();
        extraClaims.put("role", user.getRole().getRoleName());
        extraClaims.put("userId", user.getId());

        String jwtToken = jwtService.generateToken(extraClaims, new CustomUserDetails(user));

        auditLogService.log(
                user,
                null,
                "AUTHENTICATION",
                Severity.INFO,
                "USER_LOGIN",
                ipAddress,
                "User successfully logged in",
                null,
                null
        );

        return new LoginResponse(jwtToken, user.getId(), user.getRole().getRoleName());
    }
}
