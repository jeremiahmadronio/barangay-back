package com.barangay.barangay.auth.service;

import com.barangay.barangay.audit.service.AuditLogService;
import com.barangay.barangay.auth.dto.*;
import com.barangay.barangay.department.model.Department;
import com.barangay.barangay.enumerated.Severity;
import com.barangay.barangay.security.CustomUserDetails;
import com.barangay.barangay.security.JwtService;
import com.barangay.barangay.admin_management.model.User;
import com.barangay.barangay.admin_management.repository.Root_AdminRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AuthenticationService {

    private final Root_AdminRepository userRepository;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final AuditLogService auditLogService;
    private final MfaService mfaService;
    private final PasswordEncoder passwordEncoder;




//login
public LoginResponse authenticate(Login request, String ipAddress) {
    authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(request.email(), request.password())
    );

    User user = userRepository.findByEmail(request.email())
            .orElseThrow(() -> new RuntimeException("User not found"));

    // MFA logic...
    String code = mfaService.generateCode();
    user.setMfaCode(code);
    user.setMfaExpiry(LocalDateTime.now().plusMinutes(5));
    userRepository.save(user);
    mfaService.sendMfaEmail(user.getEmail(), code);

    // Initial response: Wala pang token, pero may role na
    return new LoginResponse("MFA_REQUIRED", user.getId(), user.getRole().getRoleName(), null);
}
    public LoginResponse verifyMfa(MfaRequest request, String ipAddress) {
        // 1. Fetch User (Gamitin ang 'JOIN FETCH' sa repo para iwas error sa departments) [cite: 2026-03-12]
        User user = userRepository.findByEmailWithDepartments(request.email())
                .orElseThrow(() -> new RuntimeException("User not found"));


        Set<String> departments = null;
        if (user.getRole().getRoleName().equalsIgnoreCase("STAFF")) {
            departments = user.getAllowedDepartments().stream()
                    .map(Department::getName)
                    .collect(Collectors.toSet());
        }


        HashMap<String, Object> extraClaims = new HashMap<>();
        extraClaims.put("role", user.getRole().getRoleName());
        extraClaims.put("userId", user.getId());
        if (departments != null) {
            extraClaims.put("depts", departments);
        }

        String jwtToken = jwtService.generateToken(extraClaims, new CustomUserDetails(user));

        auditLogService.log(

                user, null, "AUTHENTICATION", Severity.INFO, "USER_LOGIN_SUCCESS",

                ipAddress, "User successfully logged in via MFA", null, null

        );
        return new LoginResponse(jwtToken, user.getId(), user.getRole().getRoleName(), departments);
    }



    public void initiateForgotPassword(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Email not found."));

        String code = mfaService.generateCode();
        user.setMfaCode(code);
        user.setMfaExpiry(LocalDateTime.now().plusMinutes(5));
        userRepository.save(user);

        mfaService.sendMfaEmail(user.getEmail(), code);
    }

    public void verifyResetCode(VerifyCodeRequest request) {
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new RuntimeException("User not found."));

        if (user.getMfaCode() == null || !user.getMfaCode().equals(request.code())) {
            throw new RuntimeException("The verification code is invalid. Please check and try again.");
        }

        if (user.getMfaExpiry().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("The verification code has expired. Please request a new one.");
        }

    }

    public void completePasswordReset(ResetPasswordRequest request) {
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new RuntimeException("User not found."));


        if (!request.code().equals(user.getMfaCode())) {
            throw new RuntimeException("Unauthorized reset attempt. Verification failed.");
        }

        user.setPassword(passwordEncoder.encode(request.newPassword()));

        user.setMfaCode(null);
        user.setMfaExpiry(null);
        userRepository.save(user);

        auditLogService.log(user, null, "SECURITY", Severity.WARNING, "PASSWORD_RESET_SUCCESS",
                "SYSTEM", "User reset password", null, null);
    }
}
