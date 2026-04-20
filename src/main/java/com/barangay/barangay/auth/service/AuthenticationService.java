package com.barangay.barangay.auth.service;

import com.barangay.barangay.audit.service.AuditLogService;
import com.barangay.barangay.auth.dto.*;
import com.barangay.barangay.department.model.Department;
import com.barangay.barangay.enumerated.MfaType;
import com.barangay.barangay.enumerated.Severity;
import com.barangay.barangay.enumerated.Status;
import com.barangay.barangay.security.CustomUserDetails;
import com.barangay.barangay.security.JwtService;
import com.barangay.barangay.admin_management.model.User;
import com.barangay.barangay.admin_management.repository.Root_AdminRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.*;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
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
    private final TotpService totpService;


    @Transactional(noRollbackFor = {BadCredentialsException.class, LockedException.class, DisabledException.class})
    public LoginResponse authenticate(Login request, String ipAddress) {

        User user = userRepository.findBySystemEmail(request.email())
                .orElseThrow(() -> new UsernameNotFoundException("No account found with this email"));

        if (Boolean.TRUE.equals(user.getIsLocked())) {
            if (user.getLockUntil() != null && user.getLockUntil().isAfter(LocalDateTime.now())) {
                long minutesLeft = ChronoUnit.MINUTES.between(LocalDateTime.now(), user.getLockUntil());
                minutesLeft = (minutesLeft <= 0) ? 1 : minutesLeft;

                auditLogService.log(
                        user, null, "Login Authentication", Severity.WARNING,
                        "Login attempt on locked account", ipAddress,
                        "Account is still locked until " + user.getLockUntil(), null, null
                );
                throw new LockedException("Account is temporarily locked. Try again in " + minutesLeft + " minutes.");
            }

            user.setIsLocked(false);
            user.setLockUntil(null);
            userRepository.saveAndFlush(user);
        }

        // Check if active
        if (user.getStatus() != Status.ACTIVE) {
            auditLogService.log(
                    user, null, "Login Authentication", Severity.WARNING,
                    "Login attempt on inactive account", ipAddress,
                    "Account status: " + user.getStatus(), null, null
            );
            throw new DisabledException("Account is inactive. Contact your administrator.");
        }

        // Authenticate credentials
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.email(), request.password())
            );
        } catch (BadCredentialsException e) {
            LocalDateTime now = LocalDateTime.now();
            int attempts = (user.getFailedAttempts() == null ? 0 : user.getFailedAttempts());

            if (user.getLastFailedAttempt() != null && user.getLastFailedAttempt().isBefore(now.minusMinutes(30))) {
                attempts = 1;
            } else {
                attempts++;
            }

            user.setFailedAttempts(attempts);
            user.setLastFailedAttempt(now);

            String lockMsg = null;
            if (attempts == 3) {
                user.setIsLocked(true);
                user.setLockUntil(now.plusMinutes(5));
                lockMsg = "Account locked for 5 minutes (3rd failed attempt)";
            } else if (attempts == 5) {
                user.setIsLocked(true);
                user.setLockUntil(now.plusMinutes(15));
                lockMsg = "Account locked for 15 minutes (5th failed attempt)";
            } else if (attempts >= 10) {
                user.setIsLocked(true);
                user.setLockUntil(now.plusHours(24));
                lockMsg = "Account locked for 24 hours (10th failed attempt)";
            }

            userRepository.saveAndFlush(user);

            auditLogService.log(
                    user, null, "Login Authentication", Severity.WARNING,
                    (lockMsg != null ? lockMsg : "Failed login attempt #" + attempts), ipAddress,
                    "Authentication failure", null, null
            );

            if (user.getIsLocked()) {
                throw new LockedException(lockMsg + ". Please wait before trying again.");
            }

            throw new BadCredentialsException("Invalid email or password. Attempt " + attempts + ".");
        }

        user.setFailedAttempts(0);
        user.setIsLocked(false);
        user.setLockUntil(null);
        user.setLastFailedAttempt(null);

        String code = mfaService.generateCode();
        user.setMfaCode(code);
        user.setMfaExpiry(LocalDateTime.now().plusMinutes(5));
        userRepository.save(user);

        mfaService.sendMfaEmail(user.getSystemEmail(), code);

        auditLogService.log(
                user, null, "Login Authentication", Severity.INFO,
                "MFA code sent via EMAIL after successful credential check", ipAddress,
                null, null, null
        );

        return new LoginResponse("MFA_REQUIRED", user.getId(), user.getRole().getRoleName(), null, null,user.isTotpEnabled());
    }


    @Transactional
    public LoginResponse verifyMfa(MfaRequest request, String ipAddress) {
        User user = userRepository.findByEmailWithDepartments(request.email())
                .orElseThrow(() -> new UsernameNotFoundException("No account found with this email"));

        // Branch — TOTP or Email OTP
        boolean usingTotp = Boolean.TRUE.equals(request.usedTotp());

        if (usingTotp) {
            // TOTP verification
            if (!user.isTotpEnabled() || user.getTotpSecret() == null) {
                throw new BadCredentialsException("Authenticator app is not set up for this account.");
            }
            if (!totpService.verifyCode(user.getTotpSecret(), request.code())) {
                auditLogService.log(
                        user, null, "Login Authentication", Severity.WARNING,
                        "Invalid TOTP code attempt", ipAddress,
                        "TOTP verification failed", null, null
                );
                throw new BadCredentialsException("Invalid authenticator code. Please try again.");
            }
        } else {
            // Email OTP verification
            if (user.getMfaCode() == null || !user.getMfaCode().equals(request.code())) {
                auditLogService.log(
                        user, null, "Login Authentication", Severity.WARNING,
                        "Invalid MFA code attempt", ipAddress,
                        "MFA verification failed", null, null
                );
                throw new BadCredentialsException("Invalid verification code. Please try again.");
            }
            if (user.getMfaExpiry() == null || user.getMfaExpiry().isBefore(LocalDateTime.now())) {
                auditLogService.log(
                        user, null, "Login Authentication", Severity.WARNING,
                        "Expired MFA code attempt", ipAddress,
                        "MFA code already expired", null, null
                );
                throw new BadCredentialsException("Verification code expired. Please try again.");
            }
        }

        // Clear email MFA state (safe to clear kahit TOTP ang ginamit)
        user.setMfaCode(null);
        user.setMfaExpiry(null);
        userRepository.save(user);

        // New account — force password change
        if (user.isNewAccount()) {
            auditLogService.log(
                    user, null, "Login Authentication", Severity.INFO,
                    "New account — password change required", ipAddress,
                    null, null, null
            );
            return new LoginResponse(
                    "CHANGE_PASSWORD_REQUIRED",
                    user.getId(),
                    user.getRole().getRoleName(),
                    null,
                    null,
                    user.isTotpEnabled()
            );
        }

        // Build departments
        Set<String> departments = user.getAllowedDepartments().stream()
                .map(Department::getName)
                .collect(Collectors.toSet());

        // Build JWT
        HashMap<String, Object> extraClaims = new HashMap<>();
        extraClaims.put("role", user.getRole().getRoleName());
        extraClaims.put("userId", user.getId());
        extraClaims.put("depts", departments);

        String jwtToken = jwtService.generateToken(extraClaims, new CustomUserDetails(user));

        user.setLastLoginAt(LocalDateTime.now());
        userRepository.save(user);

        auditLogService.log(
                user, null, "Login Authentication", Severity.INFO,
                "User — " + user.getUsername() + " successfully logged in via " + (usingTotp ? "TOTP" : "EMAIL"), ipAddress,
                null, null, null
        );

        return new LoginResponse(
                "SUCCESS",
                user.getId(),
                user.getRole().getRoleName(),
                departments,
                jwtToken,
                user.isTotpEnabled()
        );
    }








    @Transactional
    public LoginResponse changePasswordNewAccount(ChangePasswordRequest request, String ipAddress) {
        User user = userRepository.findByEmailWithDepartments(request.email())
                .orElseThrow(() -> new UsernameNotFoundException("No account found with this email"));

        if (!user.isNewAccount()) {
            throw new AccessDeniedException("This endpoint is for new accounts only.");
        }

        if (!request.newPassword().equals(request.confirmPassword())) {
            throw new IllegalArgumentException("Passwords do not match.");
        }

        user.setPassword(passwordEncoder.encode(request.newPassword()));
        user.setNewAccount(false);
        user.setLastLoginAt(LocalDateTime.now());
        userRepository.save(user);

        Set<String> departments = user.getAllowedDepartments().stream()
                .map(Department::getName)
                .collect(Collectors.toSet());

        HashMap<String, Object> extraClaims = new HashMap<>();
        extraClaims.put("role", user.getRole().getRoleName());
        extraClaims.put("userId", user.getId());
        extraClaims.put("depts", departments);

        String jwtToken = jwtService.generateToken(extraClaims, new CustomUserDetails(user));

        auditLogService.log(
                user,
                null,
                "Login Authentication",
                Severity.INFO,
                "User set password for new account",
                ipAddress,
                null, null, null
        );
        return new LoginResponse("SUCCESS", user.getId(), user.getRole().getRoleName(), departments, jwtToken,user.isTotpEnabled());
    }

    @Transactional
    public void initiateForgotPassword(String email) {
        User user = userRepository.findBySystemEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("No account found with this email"));

        if (user.getStatus() != Status.ACTIVE) {
            throw new DisabledException("Account is inactive. Contact your administrator.");
        }

        String code = mfaService.generateCode();
        user.setMfaCode(code);
        user.setMfaExpiry(LocalDateTime.now().plusMinutes(5));
        userRepository.save(user);

        mfaService.sendMfaEmail(user.getSystemEmail(), code);
    }

    @Transactional
    public void verifyResetCode(VerifyCodeRequest request) {
        User user = userRepository.findBySystemEmail(request.email())
                .orElseThrow(() -> new UsernameNotFoundException("No account found with this email"));

        if (user.getMfaCode() == null || !user.getMfaCode().equals(request.code())) {
            throw new BadCredentialsException("The verification code is invalid. Please check and try again.");
        }

        if (user.getMfaExpiry() == null || user.getMfaExpiry().isBefore(LocalDateTime.now())) {
            throw new BadCredentialsException("The verification code has expired. Please request a new one.");
        }
    }

    @Transactional
    public void completePasswordReset(ResetPasswordRequest request, String ipAddress) {
        User user = userRepository.findBySystemEmail(request.email())
                .orElseThrow(() -> new UsernameNotFoundException("No account found with this email"));

        if (user.getMfaCode() == null || !user.getMfaCode().equals(request.code())) {
            throw new BadCredentialsException("Unauthorized reset attempt. Verification failed.");
        }

        if (user.getMfaExpiry() == null || user.getMfaExpiry().isBefore(LocalDateTime.now())) {
            throw new BadCredentialsException("Session expired. Please restart the password reset process.");
        }

        if (!request.newPassword().equals(request.confirmPassword())) {
            throw new IllegalArgumentException("Passwords do not match.");
        }

        user.setPassword(passwordEncoder.encode(request.newPassword()));
        user.setMfaCode(null);
        user.setMfaExpiry(null);
        userRepository.save(user);

        auditLogService.log(
                user, null, "SECURITY", Severity.WARNING, "PASSWORD_RESET_SUCCESS",
                ipAddress, "User successfully reset password", null, null
        );
    }
}
