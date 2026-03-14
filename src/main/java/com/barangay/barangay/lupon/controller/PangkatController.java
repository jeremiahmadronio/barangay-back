package com.barangay.barangay.lupon.controller;

import com.barangay.barangay.audit.service.IpAddressUtils;
import com.barangay.barangay.blotter.model.Respondent;
import com.barangay.barangay.lupon.dto.ReferToLuponRequest;
import com.barangay.barangay.lupon.service.PangkatService;
import com.barangay.barangay.security.CustomUserDetails;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/lupon")
@RequiredArgsConstructor
public class PangkatController {

    private final PangkatService pangkatService;

    @PatchMapping("/refer-to-lupon/{caseId}")
    public ResponseEntity<?> referToLupon(
            @PathVariable Long caseId,
            @Valid @RequestBody ReferToLuponRequest referToLuponRequest,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            HttpServletRequest httpServletRequest
    ){
        String ipAddress = IpAddressUtils.getClientIp(httpServletRequest);
        pangkatService.processLuponReferral(caseId,referToLuponRequest,userDetails.user(),ipAddress);
        return ResponseEntity.ok("Successfully referred to Lupon");
    }
}
