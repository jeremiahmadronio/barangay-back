
package com.barangay.barangay.auth.controller;

import com.barangay.barangay.audit.service.IpAdressUtils;
import com.barangay.barangay.auth.dto.Login;
import com.barangay.barangay.auth.dto.LoginResponse;
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
        String ipAddress = IpAdressUtils.getClientIp(servletRequest);

        return ResponseEntity.ok(authenticationService.authenticate(request, ipAddress));
    }
}

