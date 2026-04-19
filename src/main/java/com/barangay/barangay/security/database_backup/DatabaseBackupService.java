package com.barangay.barangay.security.database_backup;


import com.barangay.barangay.admin_management.model.User;
import com.barangay.barangay.audit.service.AuditLogService;
import com.barangay.barangay.enumerated.Departments;
import com.barangay.barangay.enumerated.Severity;
import com.barangay.barangay.security.database_backup.dto.BackupResponseDTO;
import com.barangay.barangay.security.database_backup.dto.BackupStatsDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.support.CronExpression;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

import javax.crypto.*;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import javax.sql.DataSource;
import java.io.*;
import java.math.BigInteger;
import java.security.spec.KeySpec;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class DatabaseBackupService {

    private final S3Client s3Client;
    private final AuditLogService auditLogService;
    private final JdbcTemplate jdbcTemplate;
    private final BackupScheduleRepository backupScheduleRepository;

    @Autowired
    private DataSource dataSource;

    @Value("${spaces.bucket.name}")
    private String bucketName;

    @Value("${spring.datasource.url}")
    private String dbUrl;

    @Value("${spring.datasource.username}")
    private String dbUser;

    @Value("${spring.datasource.password}")
    private String dbPass;


    @Value("${backup.master.passphrase}")
    private String masterPassphrase;








    private void executeBackupWorkflow(String reason, String label, String passphrase, String actorName, String ipAddress, User actor) {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm"));
        String baseFileName = "brgy_ugong_backup_" + timestamp;

        File rawFile = new File("/tmp/" + baseFileName + ".sql");
        File finalFileToUpload = rawFile;

        String effectivePassphrase = (passphrase == null || passphrase.isEmpty()) ? masterPassphrase : passphrase;
        boolean isEncrypted = effectivePassphrase != null && !effectivePassphrase.isEmpty();

        try {
            // 1. DATABASE DUMP (RAW)
            String cleanHost = dbUrl.replace("jdbc:postgresql://", "").split("\\?")[0];
            String pgDumpUri = String.format("postgresql://%s:%s@%s", dbUser, dbPass, cleanHost);
            ProcessBuilder pb = new ProcessBuilder("pg_dump", pgDumpUri, "-f", rawFile.getAbsolutePath());
            pb.redirectErrorStream(true);
            Process process = pb.start();
            if (!process.waitFor(5, TimeUnit.MINUTES) || process.exitValue() != 0) {
                throw new RuntimeException("Database dump failed.");
            }

            // 2. ENCRYPTION (ON-THE-FLY)
            if (isEncrypted) {
                File encryptedFile = new File("/tmp/" + baseFileName + ".sql.enc");
                encryptFile(rawFile, encryptedFile, effectivePassphrase);
                finalFileToUpload = encryptedFile;
            }

            Map<String, String> metadata = new HashMap<>();
            metadata.put("label", label);
            metadata.put("is-encrypted", String.valueOf(isEncrypted));
            metadata.put("created-by", actorName);
            metadata.put("reason", reason);

            // 4. CLOUD UPLOAD (With Metadata)
            String finalFileName = finalFileToUpload.getName();
            s3Client.putObject(PutObjectRequest.builder()
                            .bucket(bucketName)
                            .key(finalFileName)
                            .metadata(metadata)
                            .acl(ObjectCannedACL.PRIVATE)
                            .build(),
                    RequestBody.fromFile(finalFileToUpload));

            // 5. AUDIT LOG ONLY (No more repository.save!)
            auditLogService.log(actor, Departments.SYSTEM_ADMIN, "Database Backup", Severity.INFO,
                    "Database Backup Created (Stateless)", ipAddress,
                    "File: " + finalFileName + " | Label: " + label, null, null);

        } catch (Exception e) {
            log.error("Backup failed: {}", e.getMessage());
            throw new RuntimeException(e.getMessage());
        } finally {
            if (rawFile.exists()) rawFile.delete();
            if (isEncrypted && finalFileToUpload.exists()) finalFileToUpload.delete();
        }
    }

    private void encryptFile(File inputFile, File outputFile, String passphrase) throws Exception {
        byte[] salt = "StaticSalt_BrgyUgong_2026".getBytes();
        SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
        KeySpec spec = new PBEKeySpec(passphrase.toCharArray(), salt, 65536, 256);
        SecretKey temporaryKey = factory.generateSecret(spec);
        SecretKeySpec secretKey = new SecretKeySpec(temporaryKey.getEncoded(), "AES");

        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.ENCRYPT_MODE, secretKey);

        try (FileInputStream inputStream = new FileInputStream(inputFile);
             FileOutputStream outputStream = new FileOutputStream(outputFile);
             CipherOutputStream cipherOutputStream = new CipherOutputStream(outputStream, cipher)) {

            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                cipherOutputStream.write(buffer, 0, bytesRead);
            }
        }
    }

    @Async
    @Transactional
    public void performManualBackup(String reason, String label, String passphrase, User actor, String ipAddress) {
        executeBackupWorkflow(reason, label, passphrase, actor.getUsername(), ipAddress, actor);
    }

    public void performScheduledBackup() {
        log.info("AUTO-EXECUTE: Triggering backup workflow from Dynamic Scheduler...");
        executeBackupWorkflow("System Automated Backup", "Automated-Dynamic", null, "SYSTEM", "127.0.0.1", null);
    }




    public List<BackupResponseDTO> listAvailableBackups() {
        ListObjectsV2Request listRequest = ListObjectsV2Request.builder()
                .bucket(bucketName)
                .build();

        return s3Client.listObjectsV2(listRequest).contents().stream()
                .map(s3Object -> {
                    HeadObjectRequest headRequest = HeadObjectRequest.builder()
                            .bucket(bucketName)
                            .key(s3Object.key())
                            .build();

                    HeadObjectResponse headResponse = s3Client.headObject(headRequest);

                    return BackupResponseDTO.builder()
                            .fileName(s3Object.key())
                            .label(headResponse.metadata().get("label"))
                            .createdAt(LocalDateTime.ofInstant(s3Object.lastModified(), ZoneId.of("Asia/Manila")))                            .fileSizeKb(s3Object.size() / 1024)
                            .isEncrypted(Boolean.parseBoolean(headResponse.metadata().get("is-encrypted")))
                            .build();
                }).toList();
    }



    @Scheduled(cron = "0 0 1 * * *")
    public void cleanupOldBackups() {
        log.info("CLEANUP SOP: Starting automated cleanup of old backups...");
        LocalDateTime threshold = LocalDateTime.now().minus(365, ChronoUnit.DAYS);
        int deleted = 0;
        int failed = 0;

        try {
            ListObjectsV2Request listRequest = ListObjectsV2Request.builder()
                    .bucket(bucketName)
                    .build();

            ListObjectsV2Response listResponse = s3Client.listObjectsV2(listRequest);

            for (S3Object s3Object : listResponse.contents()) {
                try {
                    HeadObjectResponse head = s3Client.headObject(
                            HeadObjectRequest.builder()
                                    .bucket(bucketName)
                                    .key(s3Object.key())
                                    .build()
                    );

                    Instant lastModified = s3Object.lastModified();
                    LocalDateTime fileDate = LocalDateTime.ofInstant(lastModified, ZoneId.systemDefault());

                    if (fileDate.isBefore(threshold)) {
                        s3Client.deleteObject(DeleteObjectRequest.builder()
                                .bucket(bucketName)
                                .key(s3Object.key())
                                .build());

                        log.info("CLEANUP SOP: Deleted old backup -> {} | Date: {}", s3Object.key(), fileDate);
                        deleted++;
                    }

                } catch (Exception e) {
                    log.error("CLEANUP SOP: Failed to process {} -> {}", s3Object.key(), e.getMessage());
                    failed++;
                }
            }

        } catch (Exception e) {
            log.error("CLEANUP SOP: Failed to list S3 objects -> {}", e.getMessage());
        }

        log.info("CLEANUP SOP DONE: {} deleted, {} failed.", deleted, failed);
    }


    public void deleteSpecificBackup(String fileName, String passphrase, String reason, User actor, String ipAddress) {
        validatePassphrase(passphrase);

        try {
            s3Client.deleteObject(DeleteObjectRequest.builder()
                    .bucket(bucketName)
                    .key(fileName)
                    .build());

            log.info("Backup successfully removed from cloud: {}", fileName);

            auditLogService.log(actor, Departments.SYSTEM_ADMIN, "Database Management",
                    Severity.WARNING, "Manual Deletion of Backup", ipAddress,
                    "File: " + fileName + " | Reason: " + reason, null, null);

        } catch (Exception e) {
            log.error("Cloud deletion failed for {}: {}", fileName, e.getMessage());
            throw new RuntimeException("Cloud storage error: Can't delete.");
        }
    }




    public BackupStatsDTO getBackupDashboardStats() {
        ListObjectsV2Response listResponse = s3Client.listObjectsV2(
                ListObjectsV2Request.builder().bucket(bucketName).build());

        long totalSizeBytes = listResponse.contents().stream()
                .mapToLong(S3Object::size)
                .sum();
        double storageUsedGb = (double) totalSizeBytes / (1024.0 * 1024.0 * 1024.0);

        S3Object latestFile = listResponse.contents().stream()
                .max(Comparator.comparing(S3Object::lastModified))
                .orElse(null);

        String lastDateIso = (latestFile != null) ? latestFile.lastModified().toString() : null;

        BackupSchedule schedule = backupScheduleRepository.findById(1L).orElse(null);
        String nextBackupIso = null;
        String freq = "Disabled";

        if (schedule != null && schedule.isEnabled()) {
            freq = schedule.getFrequency();
            nextBackupIso = calculateNextRunIso(schedule);
        }

        return BackupStatsDTO.builder()
                .storageUsedGb(storageUsedGb)
                .storageLimitGb(10.0)
                .autoBackupFrequency(freq)
                .nextBackupTime(nextBackupIso) // ISO String para sa React
                .lastBackupStatus(latestFile != null ? "Success" : "No Backups")
                .lastBackupDate(lastDateIso)   // ISO String para sa React
                .build();
    }

    private String calculateNextRunIso(BackupSchedule config) {
        String cron = String.format("0 %d %d * * %s",
                config.getMinute(), config.getHour(),
                "WEEKLY".equals(config.getFrequency()) ? config.getDayOfWeek() : "*");

        CronExpression ce = CronExpression.parse(cron);
        ZonedDateTime next = ce.next(ZonedDateTime.now(ZoneId.of("Asia/Manila")));
        return (next != null) ? next.toInstant().toString() : null;
    }



    private String formatHumanReadableDate(Instant instant) {
        if (instant == null) return "Never";

        LocalDateTime fileDate = LocalDateTime.ofInstant(instant, ZoneId.systemDefault());
        LocalDateTime now = LocalDateTime.now();

        if (fileDate.toLocalDate().equals(now.toLocalDate())) {
            return "Today";
        } else if (fileDate.toLocalDate().equals(now.toLocalDate().minusDays(1))) {
            return "Yesterday";
        } else {
            return fileDate.format(DateTimeFormatter.ofPattern("MMM dd, yyyy"));
        }
    }

    private String calculateNextRun(BackupSchedule config) {
        try {
            String cron;
            if ("WEEKLY".equalsIgnoreCase(config.getFrequency())) {
                cron = String.format("0 %d %d * * %s",
                        config.getMinute(), config.getHour(), config.getDayOfWeek());
            } else {
                cron = String.format("0 %d %d * * *",
                        config.getMinute(), config.getHour());
            }

            CronExpression ce = CronExpression.parse(cron);
            LocalDateTime next = ce.next(LocalDateTime.now());

            if (next == null) return "N/A";

            if (next.toLocalDate().equals(LocalDate.now())) {
                return "Today at " + next.format(DateTimeFormatter.ofPattern("HH:mm"));
            }
            return next.format(DateTimeFormatter.ofPattern("MMM dd at HH:mm"));

        } catch (Exception e) {
            log.error("Failed to calculate next run: {}", e.getMessage());
            return "Invalid Schedule";
        }
    }


    private InputStream decryptStream(InputStream encryptedData, String passphrase) throws Exception {
        byte[] salt = "StaticSalt_BrgyUgong_2026".getBytes();
        SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
        KeySpec spec = new PBEKeySpec(passphrase.toCharArray(), salt, 65536, 256);
        SecretKey tmp = factory.generateSecret(spec);
        SecretKeySpec secretKey = new SecretKeySpec(tmp.getEncoded(), "AES");

        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.DECRYPT_MODE, secretKey);

        return new javax.crypto.CipherInputStream(encryptedData, cipher);
    }

    public Resource getDecryptedBackupResource(String fileName, String passphrase, User actor, String ipAddress) {
        try {
            String effectivePassphrase = (passphrase == null || passphrase.isEmpty()) ? masterPassphrase : passphrase;

            ResponseInputStream<GetObjectResponse> s3Stream = s3Client.getObject(GetObjectRequest.builder()
                    .bucket(bucketName)
                    .key(fileName)
                    .build());

            InputStream decryptedStream = decryptStream(s3Stream, effectivePassphrase);

            auditLogService.log(actor, Departments.SYSTEM_ADMIN, "Database Management",
                    Severity.INFO, "Backup Downloaded & Decrypted", ipAddress,
                    "File: " + fileName, null, null);

            return new InputStreamResource(decryptedStream);

        } catch (Exception e) {
            log.error("Decryption download failed for {}: {}", fileName, e.getMessage());
            throw new RuntimeException("Selyadong mali ang passphrase o corrupted ang file sa cloud.");
        }
    }



    private void validatePassphrase(String inputPassphrase) {
        if (inputPassphrase == null || inputPassphrase.isEmpty()) {
            throw new RuntimeException("Passphrase is required to perform a restore.");
        }
    }


    public void restoreDatabase(String fileName, String passphrase, User actor, String ipAddress) {
        validatePassphrase(passphrase);

        String effectivePassphrase = passphrase;

        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm"));
        File decryptedSql = new File("/tmp/restore_op_" + timestamp + ".sql");

        try {
            log.info("RESTORE SOP [1/5]: Creating safety backup of current state...");
            try {
                executeBackupWorkflow("Pre-Restore Safety Snapshot", "Auto-Safety", null, actor.getUsername(), ipAddress, actor);
            } catch (Exception e) {
                log.error("SAFETY BACKUP FAILED: {}", e.getMessage());
                throw new RuntimeException("Restore aborted: Hindi makagawa ng safety backup.");
            }

            log.info("RESTORE SOP [2/5]: Reading S3 metadata for {}...", fileName);
            HeadObjectResponse headResponse = s3Client.headObject(
                    HeadObjectRequest.builder().bucket(bucketName).key(fileName).build()
            );
            Map<String, String> metadata = headResponse.metadata();
            boolean isEncryptedFromMetadata = Boolean.parseBoolean(metadata.getOrDefault("is-encrypted", "false"));
            boolean isEncryptedFromFilename = fileName.endsWith(".enc");
            boolean isEncrypted = isEncryptedFromMetadata || isEncryptedFromFilename;
            log.info("File: {} | Encrypted (metadata={}; filename={})", fileName, isEncryptedFromMetadata, isEncryptedFromFilename);

            log.info("RESTORE SOP [3/5]: Downloading {} from S3...", fileName);
            ResponseInputStream<GetObjectResponse> s3Object = s3Client.getObject(
                    GetObjectRequest.builder().bucket(bucketName).key(fileName).build()
            );

            if (isEncrypted) {
                File encryptedTmp = new File("/tmp/encrypted_tmp_" + timestamp + ".sql.enc");
                try (OutputStream os = new FileOutputStream(encryptedTmp)) {
                    s3Object.transferTo(os);
                }
                decryptFile(encryptedTmp, decryptedSql, effectivePassphrase);
                encryptedTmp.delete();
            } else {
                try (OutputStream os = new FileOutputStream(decryptedSql)) {
                    s3Object.transferTo(os);
                }
            }

            log.info("RESTORE SOP [4/5]: Validating schema compatibility...");
            validateSchemaCompatibility(decryptedSql);

            log.warn("RESTORE SOP [5/5]: Wiping schema and injecting backup SQL...");
            //terminateActiveConnections();

            String cleanHost = dbUrl.replace("jdbc:postgresql://", "").split("\\?")[0];
            String psqlUri = String.format("postgresql://%s:%s@%s", dbUser, dbPass, cleanHost);

            boolean wiped = runPsql(psqlUri, null,
                    "DROP SCHEMA public CASCADE; CREATE SCHEMA public; " +
                            "GRANT ALL ON SCHEMA public TO postgres; GRANT ALL ON SCHEMA public TO public;",
                    false);
            if (!wiped) {
                throw new RuntimeException("Schema wipe failed. Database untouched — restore aborted.");
            }

            boolean restored = runPsql(psqlUri, decryptedSql.getAbsolutePath(), null, true);
            if (!restored) {
                throw new RuntimeException("SQL injection failed. System may be in degraded state. " +
                        "Use the safety backup to recover.");
            }

            log.info("RESTORE COMPLETE: Selyado na!");
            auditLogService.log(actor, Departments.SYSTEM_ADMIN, "Database Management",
                    Severity.CRITICAL, "Successful System Restore", ipAddress,
                    "Restored from: " + fileName + " | Was Encrypted: " + isEncrypted, null, null);

        } catch (Exception e) {
            log.error("RESTORE SOP [FAILED]: {}", e.getMessage());
            throw new RuntimeException("Critical Restore Failure: " + e.getMessage());
        } finally {
            if (decryptedSql.exists()) {
                boolean deleted = decryptedSql.delete();
                log.info("Temp SQL file cleaned: {}", deleted);
            }
        }
    }


    private void validateSchemaCompatibility(File decryptedSql) throws IOException {
        Set<String> backupTables = new HashSet<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(decryptedSql))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.startsWith("CREATE TABLE")) {
                    String[] parts = line.split("\\s+");
                    if (parts.length >= 3) {
                        String tableName = parts[2].replace("public.", "").replace("(", "").trim();
                        backupTables.add(tableName.toLowerCase());
                    }
                }
            }
        }

        if (backupTables.isEmpty()) {
            throw new RuntimeException("Schema validation failed: Walang nahanap na CREATE TABLE sa backup file. Posibleng corrupt o maling file.");
        }

        List<String> currentTables = jdbcTemplate.queryForList(
                "SELECT table_name FROM information_schema.tables WHERE table_schema = 'public' AND table_type = 'BASE TABLE'",
                String.class
        );

        Set<String> currentTableSet = currentTables.stream()
                .map(String::toLowerCase)
                .collect(Collectors.toSet());

        Set<String> missingInBackup = new HashSet<>(currentTableSet);
        missingInBackup.removeAll(backupTables);

        Set<String> newInBackup = new HashSet<>(backupTables);
        newInBackup.removeAll(currentTableSet);

        if (!missingInBackup.isEmpty()) {
            log.warn("SCHEMA VALIDATION: Tables in current DB missing in backup: {}", missingInBackup);
        }
        if (!newInBackup.isEmpty()) {
            log.warn("SCHEMA VALIDATION: New tables in backup not in current DB: {}", newInBackup);
        }

        int totalCurrent = currentTableSet.size();
        int matched = totalCurrent - missingInBackup.size();
        double matchRate = totalCurrent == 0 ? 1.0 : (double) matched / totalCurrent;

        if (matchRate < 0.7) {
            throw new RuntimeException(String.format(
                    "Schema validation failed: %.0f%% lang ang matched. Posibleng ibang sistema o version ang backup file.",
                    matchRate * 100));
        }

        log.info("SCHEMA VALIDATION PASSED: {}/{} tables matched.", matched, totalCurrent);
    }


    private void terminateActiveConnections() {
        try {
            jdbcTemplate.execute("""
            SELECT pg_terminate_backend(pid)
            FROM pg_stat_activity
            WHERE datname = current_database()
              AND pid <> pg_backend_pid()
              AND usename = current_user -- Selyado: Protektahan ang system users
              AND state = 'idle'         -- Patayin lang ang mga nakatambay
            """);
            log.info("Terminated idle DB connections safely.");
        } catch (Exception e) {
            log.warn("Could not terminate connections: {}", e.getMessage());
        }
    }

    private boolean runPsql(String psqlUri, String filePath, String inlineCommand, boolean singleTransaction) throws IOException, InterruptedException {
        List<String> command = new ArrayList<>(List.of("psql", psqlUri));

        if (singleTransaction) {
            command.add("--single-transaction");
        }

        command.addAll(List.of("--variable", "ON_ERROR_STOP=1"));

        if (inlineCommand != null) {
            command.add("-c");
            command.add(inlineCommand);
        }
        if (filePath != null) {
            command.add("-f");
            command.add(filePath);
        }

        ProcessBuilder pb = new ProcessBuilder(command);
        pb.redirectErrorStream(true);
        Process process = pb.start();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            reader.lines().forEach(line -> log.info("PSQL ENGINE: {}", line));
        }

        boolean finished = process.waitFor(15, TimeUnit.MINUTES);
        return finished && process.exitValue() == 0;
    }




    public void restoreFromUploadedFile(MultipartFile file, String passphrase, User actor, String ipAddress) {
        // Selyado: Gamitin ang masterPassphrase kung empty ang input para sa auto-decrypt
        String effectivePassphrase = (passphrase == null || passphrase.isEmpty()) ? masterPassphrase : passphrase;

        String originalFileName = file.getOriginalFilename();
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm"));

        // STEP 1: Updated Validation (Tanggap na ang .enc)
        if (originalFileName == null || (!originalFileName.endsWith(".sql") && !originalFileName.endsWith(".sql.enc"))) {
            throw new RuntimeException("Invalid file type. Selyadong .sql o .sql.enc lang ang tinatanggap.");
        }

        boolean isEncrypted = originalFileName.endsWith(".enc");
        File uploadedTmp = new File("/tmp/upload_raw_" + timestamp + (isEncrypted ? ".sql.enc" : ".sql"));
        File decryptedSql = new File("/tmp/manual_decrypted_" + timestamp + ".sql");

        try {
            // STEP 2: Safety Backup (Standard Operating Procedure)
            log.info("MANUAL RESTORE [1/5]: Creating safety backup of current state...");
            executeBackupWorkflow("Pre-Manual-Restore Safety", "Auto-Safety", null, actor.getUsername(), ipAddress, actor);

            // STEP 3: Save Uploaded File to Temp
            log.info("MANUAL RESTORE [2/5]: Saving uploaded file to disk...");
            try (OutputStream os = new FileOutputStream(uploadedTmp)) {
                file.getInputStream().transferTo(os);
            }

            File fileToExecute = uploadedTmp;

            // STEP 4: Smart Decryption (If .enc)
            if (isEncrypted) {
                log.info("MANUAL RESTORE [3/5]: Decrypting uploaded .sql.enc file...");
                // Siguraduhin na may decryptFile method ka (yung ginawa natin kanina)
                decryptFile(uploadedTmp, decryptedSql, effectivePassphrase);
                fileToExecute = decryptedSql;
            }

            // STEP 5: Schema Validation (Dapat sa decrypted file ito i-check)
            log.info("MANUAL RESTORE [4/5]: Validating schema compatibility...");
            validateSchemaCompatibility(fileToExecute);

            // STEP 6: Terminate, Wipe & Restore
            log.warn("MANUAL RESTORE [5/5]: Wiping schema and injecting SQL...");
            terminateActiveConnections();

            String cleanHost = dbUrl.replace("jdbc:postgresql://", "").split("\\?")[0];
            String psqlUri = String.format("postgresql://%s:%s@%s", dbUser, dbPass, cleanHost);

            // Wipe Database
            boolean wiped = runPsql(psqlUri, null,
                    "DROP SCHEMA public CASCADE; CREATE SCHEMA public; " +
                            "GRANT ALL ON SCHEMA public TO postgres; GRANT ALL ON SCHEMA public TO public;",
                    false);

            if (!wiped) throw new RuntimeException("Schema wipe failed. Database untouched.");

            // Inject Data
            boolean restored = runPsql(psqlUri, fileToExecute.getAbsolutePath(), null, true);
            if (!restored) throw new RuntimeException("SQL injection failed. System state: DEGRADED.");

            log.info("MANUAL RESTORE COMPLETE: Selyado na!");

            // Audit Log
            auditLogService.log(actor, Departments.SYSTEM_ADMIN, "Database Management",
                    Severity.CRITICAL, "Successful Manual Restore", ipAddress,
                    "File: " + originalFileName + " | Decrypted: " + isEncrypted, null, null);

        } catch (Exception e) {
            log.error("MANUAL RESTORE FAILED: {}", e.getMessage());
            throw new RuntimeException("Restore Failure: " + e.getMessage());
        } finally {
            if (uploadedTmp.exists()) uploadedTmp.delete();
            if (decryptedSql.exists()) decryptedSql.delete();
            log.info("Cleanup of temporary restore files completed.");
        }
    }














    private void decryptFile(File inputFile, File outputFile, String passphrase) throws Exception {
        byte[] salt = "StaticSalt_BrgyUgong_2026".getBytes();
        SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
        KeySpec spec = new PBEKeySpec(passphrase.toCharArray(), salt, 65536, 256);
        SecretKey temporaryKey = factory.generateSecret(spec);
        SecretKeySpec secretKey = new SecretKeySpec(temporaryKey.getEncoded(), "AES");

        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.DECRYPT_MODE, secretKey);

        try (FileInputStream inputStream = new FileInputStream(inputFile);
             CipherInputStream cipherInputStream = new CipherInputStream(inputStream, cipher);
             FileOutputStream outputStream = new FileOutputStream(outputFile)) {

            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = cipherInputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }
        }
    }
}