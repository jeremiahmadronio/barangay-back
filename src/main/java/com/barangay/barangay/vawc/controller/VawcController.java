package com.barangay.barangay.vawc.controller;

import com.barangay.barangay.admin_management.model.User;
import com.barangay.barangay.audit.service.IpAddressUtils;
import com.barangay.barangay.blotter.dto.complaint.EvidenceOptionDTO;
import com.barangay.barangay.security.CustomUserDetails;
import com.barangay.barangay.vawc.dto.*;
import com.barangay.barangay.vawc.service.ComplaintService;
import com.barangay.barangay.vawc.service.VawcService;
import com.sun.net.httpserver.HttpServer;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/vawc")
@RequiredArgsConstructor
public class VawcController {

    private final ComplaintService complaintService;
    private final VawcService vawcService;



    @PostMapping("/complaint-entry")
    public ResponseEntity<?> complaintEntry (
           @Valid @RequestBody ComplaintDTO dto,
          @AuthenticationPrincipal CustomUserDetails actor,
            HttpServletRequest httpRequest
    ){
        String ipAddress = IpAddressUtils.getClientIp(httpRequest);
        return ResponseEntity.ok(complaintService.fileVAWCComplaint(dto,actor.user(),ipAddress));

    }

    @GetMapping("/evidence-options")
    public ResponseEntity<List<EvidenceOptionDTO>> getEvidenceOptions() {
        return ResponseEntity.ok(complaintService.getEvidenceOptions());
    }


    @GetMapping("/violence-options")
    public ResponseEntity<List<ViolenceOptionDTO>> getOptions() {
        return ResponseEntity.ok(complaintService.getViolenceOptions());
    }

    @GetMapping("/case-summary")
    public ResponseEntity<Page<CaseSummaryDTO>> getVAWCCases(
            @AuthenticationPrincipal CustomUserDetails officer,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String violenceType,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateTo,
            Pageable pageable
    ) {
        Page<CaseSummaryDTO> summary = vawcService.getVAWCSummary(
                officer.user(),
                search,
                status,
                violenceType,
                dateFrom,
                dateTo,
                pageable
        );
        return ResponseEntity.ok(summary);
    }


    @GetMapping("/vawc-stats")
    public ResponseEntity<CaseStatsDTO> getStats() {
        return ResponseEntity.ok(vawcService.getVawcStats());
    }


    @GetMapping("/details/{id}")
    public ResponseEntity<CaseViewDTO> getCaseView(@PathVariable Long id) {
        return ResponseEntity.ok(vawcService.getVawcCaseDetails(id));
    }

}
