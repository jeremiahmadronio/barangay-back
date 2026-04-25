package com.barangay.barangay.auth.service;

import com.barangay.barangay.admin_management.model.User;
import com.barangay.barangay.admin_management.repository.Root_AdminRepository;
import com.barangay.barangay.audit.service.AuditLogService;
import com.barangay.barangay.auth.dto.MfaConfirmationRequest;
import com.barangay.barangay.auth.dto.MfaEnableSuccessResponse;
import com.barangay.barangay.auth.dto.MfaSetupResponse;
import com.barangay.barangay.enumerated.Severity;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MfaSetupService {

    private final Root_AdminRepository userRepository;
    private final TotpService totpService;
    private final AuditLogService auditLogService;

    // STEP 1: Generate the QR and Secret
    public MfaSetupResponse initiateTotpSetup(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Generate temporary secret
        String secret = totpService.generateSecret();
        String qrCode = totpService.generateQrCodeBase64(secret, user.getSystemEmail());

        return new MfaSetupResponse(secret, qrCode);
    }

    // STEP 2: Verify and Finalize
    @Transactional
    public MfaEnableSuccessResponse confirmAndEnableTotp(UUID userId, MfaConfirmationRequest request, String ipAddress) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        boolean isValid = totpService.verifyCode(request.secret(), request.code());
        if (!isValid) {
            throw new BadCredentialsException("Invalid verification code. Setup failed.");
        }

        user.setTotpSecret(request.secret());
        user.setTotpEnabled(true);

        Set<String> recoveryCodes = generateRecoveryCodes();
        user.setRecoveryCodes(recoveryCodes);

        userRepository.save(user);

        auditLogService.log(
                user, null, "Authentication", Severity.INFO,
                "Enabled Authenticator App", ipAddress,
                null, null, null
        );

        return new MfaEnableSuccessResponse("TOTP_ENABLED_SUCCESSFULLY", recoveryCodes);
    }

    private Set<String> generateRecoveryCodes() {
        return java.util.stream.Stream.generate(() ->
                        java.util.UUID.randomUUID().toString().substring(0, 8).toUpperCase())
                .limit(10)
                .collect(java.util.stream.Collectors.toSet());
    }
}