package com.barangay.barangay.lupon.controller;

import com.barangay.barangay.audit.service.IpAddressUtils;
import com.barangay.barangay.lupon.dto.ExtendLuponRequest;
import com.barangay.barangay.lupon.dto.LuponSummaryDTO;
import com.barangay.barangay.lupon.dto.ReferToLuponRequest;
import com.barangay.barangay.lupon.service.PangkatService;
import com.barangay.barangay.security.CustomUserDetails;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

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



    @GetMapping("/summary")
    public ResponseEntity<Page<LuponSummaryDTO>> getSummary(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Long natureId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        return ResponseEntity.ok(pangkatService.getLuponSummary(search, natureId, startDate, endDate, page, size));
    }


    @PatchMapping("/extend/{caseId}")
    public ResponseEntity<?> extendPeriod(
            @PathVariable Long caseId,
            @Valid @RequestBody ExtendLuponRequest request,
            @AuthenticationPrincipal CustomUserDetails officer,
            HttpServletRequest servletRequest) {

        String ipAddress = IpAddressUtils.getClientIp(servletRequest);
        pangkatService.processExtension(caseId, request, officer.user(), ipAddress);
        return ResponseEntity.ok("Case period successfully extended by 15 days.");
    }
}
