package com.barangay.barangay.audit.controller;

import com.barangay.barangay.audit.dto.AuditFilterOptions;
import com.barangay.barangay.audit.dto.AuditTable;
import com.barangay.barangay.audit.dto.AuditViewAll;
import com.barangay.barangay.audit.dto.Stats;
import com.barangay.barangay.audit.service.AuditLogService;
import com.barangay.barangay.audit.service.IpAddressUtils;
import com.barangay.barangay.users.model.User;
import com.barangay.barangay.users.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Controller
@RestController
@RequestMapping("/api/v1/audit")
@RequiredArgsConstructor
public class AuditLogsController {

    private final AuditLogService auditLogService;
    private final UserRepository  userRepository;


    @GetMapping("/stats")
    public ResponseEntity<Stats> getAuditMetrics(@RequestParam UUID actorId) {

        //checking if the user is root admin
        User user = userRepository.findById(actorId)
                .orElseThrow(() -> new RuntimeException("Actor not found."));

        if (!user.getRole().getRoleName().equals("ROOT_ADMIN")) {
            return ResponseEntity.status(403).build();        }

        return ResponseEntity.ok(auditLogService.getAuditSummary());
    }



    @GetMapping("/table")
    public ResponseEntity<Page<AuditTable>> getLogs(
            @RequestParam(required = false)          String search,
            @RequestParam(required = false)          String severity,
            @RequestParam(required = false)          String module,
            @RequestParam(required = false)          String action,
            @RequestParam(defaultValue = "0")        int page,
            @RequestParam(defaultValue = "5")        int size,
            @RequestParam UUID actorId
    ) {
        //check if user is root admin.
        User user = userRepository.findById(actorId).
                orElseThrow(() -> new RuntimeException("user not found."));

        if(!user.getRole().getRoleName().equals("ROOT_ADMIN")){
            throw new RuntimeException("Only root admin can access.");
        }

        return ResponseEntity.ok(
                auditLogService.getAuditTable(search, severity, module, action, page, size)
        );
    }


    @GetMapping("/filter-options")
    public ResponseEntity<AuditFilterOptions> getFilterOptions(
            @RequestParam UUID actorId

    ) {
        //check if user is root admin.
        User user = userRepository.findById(actorId).
                orElseThrow(() -> new RuntimeException("user not found."));

        if(!user.getRole().getRoleName().equals("ROOT_ADMIN")){
            throw new RuntimeException("Only root admin can access.");
        }
        return ResponseEntity.ok(auditLogService.getFilterOptions());
    }




    @GetMapping("/viewAll")
    public ResponseEntity<AuditViewAll> getLog(
            @RequestParam long id,
            @RequestParam UUID actorId,
            HttpServletRequest request
    ) {
        //check if user is root admin.
        User user = userRepository.findById(actorId).
                orElseThrow(() -> new RuntimeException("user not found."));

        if(!user.getRole().getRoleName().equals("ROOT_ADMIN")){
            throw new RuntimeException("Only root admin can access.");
        }
        String ipAddress = IpAddressUtils.getClientIp(request);
        return ResponseEntity.ok(auditLogService.getAuditLog(id,user,ipAddress));
    }

}
