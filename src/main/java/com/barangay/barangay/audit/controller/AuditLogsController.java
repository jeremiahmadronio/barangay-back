package com.barangay.barangay.audit.controller;

import com.barangay.barangay.audit.dto.AuditFilterOptions;
import com.barangay.barangay.audit.dto.AuditTable;
import com.barangay.barangay.audit.dto.AuditViewAll;
import com.barangay.barangay.audit.dto.Stats;
import com.barangay.barangay.audit.service.AuditLogService;
import com.barangay.barangay.audit.service.IpAddressUtils;
import com.barangay.barangay.security.CustomUserDetails;
import com.barangay.barangay.admin_management.repository.Root_AdminRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;


@Controller
@RestController
@RequestMapping("/api/v1/audit")
@RequiredArgsConstructor
public class AuditLogsController {

    private final AuditLogService auditLogService;
    private final Root_AdminRepository userRepository;


    @GetMapping("/stats")
    public ResponseEntity<Stats> getAuditMetrics() {

        return ResponseEntity.ok(auditLogService.getAuditSummary());
    }


    @GetMapping("/table")
    public ResponseEntity<Page<AuditTable>> getLogs(
            @RequestParam(required = false)                                        String    search,
            @RequestParam(required = false)                                        String    severity,
            @RequestParam(required = false)                                        String    module,
            @RequestParam(required = false)                                        String    action,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(defaultValue = "0")                                      int       page,
            @RequestParam(defaultValue = "10")                                     int       size
    ) {
        return ResponseEntity.ok(
                auditLogService.getAuditTable(search, severity, module, action, startDate, endDate, page, size)
        );
    }



    @GetMapping("/filter-options")
    public ResponseEntity<AuditFilterOptions> getFilterOptions(

    ) {

        return ResponseEntity.ok(auditLogService.getFilterOptions());
    }




    @GetMapping("/viewAll")
    public ResponseEntity<AuditViewAll> getLog(
            @RequestParam long id,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            HttpServletRequest request
    ) {

        String ipAddress = IpAddressUtils.getClientIp(request);
        return ResponseEntity.ok(auditLogService.getAuditLog(id,userDetails.user(),ipAddress));
    }

}
