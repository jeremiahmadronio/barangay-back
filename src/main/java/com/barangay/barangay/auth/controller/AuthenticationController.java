
    package com.barangay.barangay.auth.controller;

    import com.barangay.barangay.audit.service.IpAddressUtils;
    import com.barangay.barangay.auth.dto.*;
    import com.barangay.barangay.auth.service.AuthenticationService;
    import com.barangay.barangay.auth.service.MfaSetupService;
    import com.barangay.barangay.security.CustomUserDetails;
    import jakarta.servlet.http.HttpServletRequest;
    import jakarta.validation.Valid;
    import lombok.RequiredArgsConstructor;
    import org.springframework.http.ResponseEntity;
    import org.springframework.security.core.annotation.AuthenticationPrincipal;
    import org.springframework.stereotype.Controller;
    import org.springframework.web.bind.annotation.*;

    @Controller
    @RestController
    @RequestMapping("/api/v1/auth")
    @RequiredArgsConstructor
    public class AuthenticationController {

        private final AuthenticationService authenticationService;
        private final MfaSetupService mfaSetupService;

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




            @GetMapping("/setup")
            public ResponseEntity<MfaSetupResponse> getSetupDetails(
                    @AuthenticationPrincipal CustomUserDetails userDetails) {
                return ResponseEntity.ok(mfaSetupService.initiateTotpSetup(userDetails.user().getId()));
            }

            @PostMapping("/confirm")
            public ResponseEntity<MfaEnableSuccessResponse> confirmSetup(
                    @AuthenticationPrincipal CustomUserDetails userDetails,
                    @RequestBody MfaConfirmationRequest request,
                    jakarta.servlet.http.HttpServletRequest httpRequest) {

                String ipAddress = IpAddressUtils.getClientIp(httpRequest);
                MfaEnableSuccessResponse response = mfaSetupService.confirmAndEnableTotp(
                        userDetails.user().getId(),
                        request,
                        ipAddress
                );
                return ResponseEntity.ok(response);
            }


        @PostMapping("/backup-email/initiate")
        public ResponseEntity<Void> initiateBackupSetup(
                @RequestParam String primaryEmail,
                @RequestParam String backupEmail,
                HttpServletRequest servletRequest) {
            String ipAddress = IpAddressUtils.getClientIp(servletRequest);
            authenticationService.initiateBackupEmailSetup(primaryEmail, backupEmail,ipAddress);
            return ResponseEntity.ok().build();
        }

        @PostMapping("/backup-email/verify")
        public ResponseEntity<Void> verifyBackupSetup(
                @RequestParam String primaryEmail,
                @RequestParam String backupEmail,
                @RequestParam String code,
                HttpServletRequest httpRequest) {
            String ipAddress = IpAddressUtils.getClientIp(httpRequest);
            authenticationService.verifyAndSaveBackupEmail(primaryEmail, backupEmail, code, ipAddress);
            return ResponseEntity.ok().build();
        }



        @PostMapping("/forgot-password")
        public ResponseEntity<Void> forgotPassword(@RequestBody @Valid ForgotPasswordRequest request) {
            authenticationService.initiateForgotPassword(request.email());
            return ResponseEntity.ok().build();
        }

        @PostMapping("/forgot-password/verify")
        public ResponseEntity<Void> verifyCode(@RequestBody @Valid VerifyCodeRequest request) {
            authenticationService.verifyResetCode(request);
            return ResponseEntity.ok().build();
        }

        @PostMapping("/forgot-password/reset")
        public ResponseEntity<Void> resetPassword(
                @RequestBody @Valid ResetPasswordRequest request,
                HttpServletRequest httpRequest) {
            String ipAddress = IpAddressUtils.getClientIp(httpRequest);

            authenticationService.completePasswordReset(request,ipAddress);
            return ResponseEntity.ok().build();
        }

        @PostMapping("/change-password")
        public ResponseEntity<LoginResponse> changePassword(
                @RequestBody @Valid ChangePasswordRequest request,
                HttpServletRequest httpRequest) {
            String ipAddress = IpAddressUtils.getClientIp(httpRequest);
            return ResponseEntity.ok(authenticationService.changePasswordNewAccount(request, ipAddress));
        }
    }

