package com.barangay.barangay.blotter.controller;

import com.barangay.barangay.audit.service.IpAddressUtils;
import com.barangay.barangay.blotter.dto.complaint.FormalComplaintEntry;
import com.barangay.barangay.blotter.dto.complaint.RecordBlotterEntry;
import com.barangay.barangay.blotter.service.BlotterFormComplaintService;
import com.barangay.barangay.security.CustomUserDetails;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/blotter-form")
@RequiredArgsConstructor
public class BlotterFormComplaintController {

    private final BlotterFormComplaintService blotterService;

    @PostMapping("/for-the-record")
    public ResponseEntity<String> recordBlotter(
            @Valid @RequestBody RecordBlotterEntry dto,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            HttpServletRequest request
    ) {
        String ipAddress = IpAddressUtils.getClientIp(request);
        String blotterNo = blotterService.saveForTheRecord(dto, userDetails.user(), ipAddress);
        return ResponseEntity.ok(blotterNo);
    }


    @PostMapping("/formal-complaint")
    public ResponseEntity<String> formalBlotter(
           @Valid @RequestBody FormalComplaintEntry dto,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            HttpServletRequest request
    ){
        String ipAddress = IpAddressUtils.getClientIp(request);
        String blotterNo =  blotterService.fileFormalComplaint(dto, userDetails.user(), ipAddress);
        return ResponseEntity.ok(blotterNo);
    }


    @PostMapping("/escalate/{blotterNumber}")
    public ResponseEntity<?> escalateToFormalComplaint(
          @PathVariable  String blotterNumber,
            @Valid @RequestBody FormalComplaintEntry dto,
           @AuthenticationPrincipal CustomUserDetails userDetails,
            HttpServletRequest request
    ){
        String ipAddress = IpAddressUtils.getClientIp(request);
        return ResponseEntity.ok(blotterService.escalateToFormalComplaint(blotterNumber, dto, userDetails.user(), ipAddress));
    }
}