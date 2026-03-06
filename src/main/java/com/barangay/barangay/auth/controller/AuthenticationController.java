
package com.barangay.barangay.auth.controller;

import com.barangay.barangay.audit.service.IpAddressUtils;
import com.barangay.barangay.auth.dto.*;
import com.barangay.barangay.auth.service.AuthenticationService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Controller
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthenticationController {

    private final AuthenticationService authenticationService;

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(
            @Valid @RequestBody Login request,
            HttpServletRequest servletRequest
    ) {
        String ipAddress = IpAddressUtils.getClientIp(servletRequest);

        return ResponseEntity.ok(authenticationService.authenticate(request, ipAddress));
    }


    @PostMapping("/verify-mfa")
    public ResponseEntity<LoginResponse> verifyMfa(
            @Valid @RequestBody MfaRequest request,
            HttpServletRequest servletRequest
    ) {
        String ipAddress = IpAddressUtils.getClientIp(servletRequest);
        return ResponseEntity.ok(authenticationService.verifyMfa(request, ipAddress));
    }


    @PostMapping("/forgot-password")
    public ResponseEntity<String> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        authenticationService.initiateForgotPassword(request.email());
        return ResponseEntity.ok("If an account is associated with this email, a verification code has been sent.");
    }

    @PostMapping("/verify-reset-code")
    public ResponseEntity<String> verifyCode(@Valid @RequestBody VerifyCodeRequest request) {
        authenticationService.verifyResetCode(request);
        return ResponseEntity.ok("Verification successful. You may now proceed to reset your password.");
    }

    @PostMapping("/reset-password")
    public ResponseEntity<String> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        authenticationService.completePasswordReset(request);
        return ResponseEntity.ok("Password has been successfully reset. Please log in with your new credentials.");
    }

}

