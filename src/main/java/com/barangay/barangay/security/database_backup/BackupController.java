package com.barangay.barangay.security.database_backup;


import com.barangay.barangay.audit.service.IpAddressUtils;
import com.barangay.barangay.security.CustomUserDetails;
import com.barangay.barangay.security.database_backup.dto.BackupResponseDTO;
import com.barangay.barangay.security.database_backup.dto.BackupStatsDTO;
import org.springframework.core.io.Resource;
import org.springframework.core.io.InputStreamResource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/admin/backups")
@RequiredArgsConstructor
public class BackupController {

    private final DatabaseBackupService backupService;
    private final BackupScheduleRepository backupScheduleRepository;
    private final ScheduleBackupService  scheduleBackupService;


    @PostMapping("/trigger")
    public ResponseEntity<String> triggerManualBackup(
            @RequestParam(name = "label", required = false) String label,
            @RequestParam(name = "passphrase", required = false) String passphrase,
            @RequestParam(name = "reason", defaultValue = "Manual Backup") String reason,
            @AuthenticationPrincipal CustomUserDetails actor,
            HttpServletRequest request
    ) {
        String ipAddress = IpAddressUtils.getClientIp(request);

        try {
            backupService.performManualBackup(reason, label, passphrase, actor.user(), ipAddress);

            return ResponseEntity.ok("Backup process initiated successfully. " +
                    "The file will be available in the history once processing is complete.");

        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body("Failed to start backup process: " + e.getMessage());
        }
    }

    @GetMapping("/list")
    public ResponseEntity<List<BackupResponseDTO>> getBackupList() {
        return ResponseEntity.ok(backupService.listAvailableBackups());
    }


    @DeleteMapping("/delete")
    public ResponseEntity<String> deleteBackup(
            @RequestParam String fileName,
            @RequestParam String passphrase,
            @RequestParam(name = "reason", defaultValue = "Manual Cleanup") String reason,
            @AuthenticationPrincipal CustomUserDetails actor,
            HttpServletRequest request
    ) {
        String ipAddress = IpAddressUtils.getClientIp(request);

        try {
            backupService.deleteSpecificBackup(fileName, passphrase, reason, actor.user(), ipAddress);

            return ResponseEntity.ok("Backup file '" + fileName + "' successfully removed from cloud storage.");

        } catch (Exception e) {

            return ResponseEntity.internalServerError()
                    .body("Error deleting backup: " + e.getMessage());
        }
    }


    @GetMapping("/download")
    public ResponseEntity<Resource> downloadBackup(
            @RequestParam String fileName,
            @RequestParam(required = false) String passphrase,
            @AuthenticationPrincipal CustomUserDetails actor,
            HttpServletRequest request) {

        String ipAddress = IpAddressUtils.getClientIp(request);

        Resource resource = backupService.getDecryptedBackupResource(
                fileName,
                passphrase,
                actor.user(),
                ipAddress
        );

        String downloadName = fileName.replace(".enc", "");
        if (!downloadName.toLowerCase().endsWith(".sql")) {
            downloadName += ".sql";
        }

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + downloadName + "\"")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(resource);
    }


    @PostMapping("/restore")
    public ResponseEntity<String> restoreDatabase(
            @RequestParam String fileName,
            @RequestParam(required = false) String passphrase,
            @AuthenticationPrincipal CustomUserDetails actor,
            HttpServletRequest request
    ) {
        String ipAddress = IpAddressUtils.getClientIp(request);


        try {
            backupService.restoreDatabase(fileName, passphrase, actor.user(), ipAddress);

            return ResponseEntity.ok("Database restoration successful.");

        } catch (Exception e) {

            return ResponseEntity.internalServerError()
                    .body("CRITICAL ERROR: Restore not finish. " + e.getMessage());
        }


    }


    @PostMapping(value = "/restore/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<String> uploadAndRestore(
            @RequestPart("file") MultipartFile file,
            @RequestParam("passphrase") String passphrase,
            @AuthenticationPrincipal CustomUserDetails actor,
            HttpServletRequest request
    ) {
        String ipAddress = IpAddressUtils.getClientIp(request);

        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body("File is Empty.");
        }


        try {
            backupService.restoreFromUploadedFile(file, passphrase, actor.user(), ipAddress);

            return ResponseEntity.ok("Manual restoration successful. ");

        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body("Critical Error during manual restore: " + e.getMessage());
        }
    }





    @PostMapping("/settings/schedule")
    public ResponseEntity<String> updateSchedule(@RequestBody BackupSchedule newSchedule) {
        newSchedule.setId(1L);
        backupScheduleRepository.save(newSchedule);

        scheduleBackupService.refreshSchedule();

        return ResponseEntity.ok("Schedule updated! Ang backup ay backup run " +
                newSchedule.getFrequency() + " " + newSchedule.getHour() + ":" + newSchedule.getMinute());
    }


    @GetMapping("/stats")
    public ResponseEntity<BackupStatsDTO> getDashboardStats() {
        try {
            BackupStatsDTO stats = backupService.getBackupDashboardStats();
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }



}