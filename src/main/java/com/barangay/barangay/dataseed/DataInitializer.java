package com.barangay.barangay.dataseed;

import com.barangay.barangay.audit.model.AuditLog;
import com.barangay.barangay.audit.repository.AuditLogRepository;
import com.barangay.barangay.blotter.model.*;
import com.barangay.barangay.blotter.model.EvidenceType;
import com.barangay.barangay.blotter.repository.*;
import com.barangay.barangay.department.model.Department;
import com.barangay.barangay.enumerated.*;
import com.barangay.barangay.permission.model.Permission;
import com.barangay.barangay.resident.model.*;
import com.barangay.barangay.resident.repository.PeopleRepository;
import com.barangay.barangay.resident.repository.ResidentRepository;
import com.barangay.barangay.role.model.Role;
import com.barangay.barangay.admin_management.model.User;
import com.barangay.barangay.department.repository.DepartmentRepository;
import com.barangay.barangay.permission.repository.PermissionRepository;
import com.barangay.barangay.role.repository.RoleRepository;
import com.barangay.barangay.admin_management.repository.Root_AdminRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final RoleRepository roleRepository;
    private final Root_AdminRepository userRepository;
    private final PermissionRepository permissionRepository;
    private final DepartmentRepository departmentRepository;
    private final AuditLogRepository auditLogRepository;
    private final JdbcTemplate jdbcTemplate;
    private final PasswordEncoder passwordEncoder;

    //blotter
    private final NatureOfComplaintRepository natureOfComplaintRepository;
    private final RelationshipTypeRepository relationshipTypeRepository;
    private final IncidentFrequencyRepository incidentFrequencyRepository;
    private final EvidenceTypeRepository evidenceTypeRepository;

    private final BlotterCaseRepository blotterCaseRepository;
    private final CasteTimeLineRepository caseTimelineRepository;

    private final PeopleRepository peopleRepository;
    private final ResidentRepository residentRepository;




    @Override
    @Transactional
    public void run(String... args) {

        if (residentRepository.count() == 0) {
            seedResidents(100);
        }


        // ── Departments ──────────────────────────────────────────────────────
        Department adminDept = createDeptIfNotFound("ADMINISTRATION");
        createDeptIfNotFound("VAWC");
        createDeptIfNotFound("BLOTTER");
        createDeptIfNotFound("KAPITANA");
        createDeptIfNotFound("BCPC");
        createDeptIfNotFound("CLEARANCE");
        createDeptIfNotFound("LUPONG_TAGAPAMAYAPA");
        createDeptIfNotFound("OPERATIONAL_STAFF");
        createDeptIfNotFound("FTJS");
        createDeptIfNotFound("ROOT_ADMIN");
        createDeptIfNotFound("CONTENT");

        // ── Permissions ──────────────────────────────────────────────────────

        // BLOTTER MODULE PERMISSIONS
      createPermIfNotFound("View Blotter Records");
         createPermIfNotFound("Create Blotter Entry");
         createPermIfNotFound("Delete Blotter Records");
         createPermIfNotFound("Manage Hearings & Mediation");
         createPermIfNotFound("Update Case Status");
        createPermIfNotFound("Issue CFA & Certifications");
       createPermIfNotFound("Generate Blotter Reports");



        createEvidenceTypeIfNotFound("Medical Certificate");
        createEvidenceTypeIfNotFound("Medico-Legal Report");
        createEvidenceTypeIfNotFound("Psychological Evaluation Report");
        createEvidenceTypeIfNotFound("Dental Record");
        createEvidenceTypeIfNotFound("Lab Result (Toxicology/X-Ray/etc.)");

        // 2. Testimonial
        createEvidenceTypeIfNotFound("Affidavit / Sworn Statement");
        createEvidenceTypeIfNotFound("Complaint Affidavit");
        createEvidenceTypeIfNotFound("Witness Statement");
        createEvidenceTypeIfNotFound("Kagawad/Lupon Mediation Agreement");
        createEvidenceTypeIfNotFound("Police Blotter/Report Referral");
        createEvidenceTypeIfNotFound("Court Order / Subpoena");

        // 3. Digital
        createEvidenceTypeIfNotFound("Screenshot (Messenger/Viber/WhatsApp)");
        createEvidenceTypeIfNotFound("Screenshot (Facebook/Post/Comment)");
        createEvidenceTypeIfNotFound("Voice/Audio Recording");
        createEvidenceTypeIfNotFound("Email Print-out");
        createEvidenceTypeIfNotFound("SMS / Text Message Transcript");

        // 4. Visual
        createEvidenceTypeIfNotFound("Photograph (Incident Scene)");
        createEvidenceTypeIfNotFound("Photograph (Physical Injury)");
        createEvidenceTypeIfNotFound("Photograph (Property Damage)");
        createEvidenceTypeIfNotFound("CCTV Footage (Digital/USB)");
        createEvidenceTypeIfNotFound("Dashcam Footage");
        createEvidenceTypeIfNotFound("Hand-drawn Map or Sketch");

        // 5. Material
        createEvidenceTypeIfNotFound("Physical Item (Weapon/Tool/etc.)");
        createEvidenceTypeIfNotFound("Clothing or Personal Belonging");
        createEvidenceTypeIfNotFound("Illegal Substance (Turned over to Police)");
        createEvidenceTypeIfNotFound("Actual Damaged Equipment/Part");

        // 6. Financial
        createEvidenceTypeIfNotFound("Official Receipt / Proof of Payment");
        createEvidenceTypeIfNotFound("Contract / Lease Agreement");
        createEvidenceTypeIfNotFound("Land Title / Deed of Sale");

        createEvidenceTypeIfNotFound("Other Supporting Documents/Items");

        createNatureIfNotFound("Physical Injury");
        createNatureIfNotFound("Slander / Oral Defamation");
        createNatureIfNotFound("Theft");
        createNatureIfNotFound("Threats");
        createNatureIfNotFound("Trespassing");
        createNatureIfNotFound("Grave Coercion");
        createNatureIfNotFound("Debt / Financial Dispute");
        createNatureIfNotFound("Unjust Vexation");
        createNatureIfNotFound("Boundary / Land Dispute");
        createNatureIfNotFound("Family / Relational Dispute");
        createNatureIfNotFound("Noise Nuisance (Videoke, Loud Music)");
        createNatureIfNotFound("Animal Nuisance (Stray/Noise/Waste)");
        createNatureIfNotFound("Public Disturbance / Scandal");
        createNatureIfNotFound("Illegal Parking / Obstruction");
        createNatureIfNotFound("Violation of Barangay Ordinance");
        createNatureIfNotFound("Others (Specify in Narrative)");


        // ── Relationship Types (Reference Data) ──────────────────────────────
        createRelationIfNotFound("Neighbor");
        createRelationIfNotFound("Relative");
        createRelationIfNotFound("Family Member");
        createRelationIfNotFound("Business Partner");
        createRelationIfNotFound("Former Spouse / Partner");
        createRelationIfNotFound("Stranger");
        createRelationIfNotFound("Landlord / Tenant");

        // ── Incident Frequencies (Reference Data) ────────────────────────────
        createFrequencyIfNotFound("First Time");
        createFrequencyIfNotFound("Second Time");
        createFrequencyIfNotFound("Habitual / Third Time+");

        // ── Roles ────────────────────────────────────────────────────────────
        Role rootRole = roleRepository.findByRoleName("ROOT_ADMIN")
                .orElseGet(() -> {
                    Role role = new Role();
                    role.setRoleName("ROOT_ADMIN");
                    return roleRepository.save(role);
                });

        createRoleIfNotFound("ADMIN");
        createRoleIfNotFound("STAFF");


        String rawPassword = "82219800Jeremiah!";
        String hashedContext = passwordEncoder.encode(rawPassword);

        User rootUser = userRepository.findByUsername("rootadmin")
                .orElseGet(() -> {
                    User root = new User();
                    root.setUsername("rootadmin");
                    root.setPassword(hashedContext);
                    root.setEmail("nermamadronio@gmail.com");
                    root.setFirstName("Juan");
                    root.setLastName("Dela Cruz");
                    root.setStatus(Status.ACTIVE);
                    root.setRole(rootRole);
                    root.setAllowedDepartments(new HashSet<>(Set.of(adminDept)));
                    root.setFailedAttempts(0);
                    root.setIsLocked(false);
                    return userRepository.save(root);
                });

        // ── Audit Log Seeding ────────────────────────────────────────────────
        if (auditLogRepository.count() == 0) {
            System.out.println("Starting Audit Log Seeding for Dashboard Testing...");

            // FIX: Each seedLogs call now uses the correct department enum
            seedLogs(rootUser, Departments.VAWC,      "VAWC",      245);
            seedLogs(rootUser, Departments.BLOTTER,   "BLOTTER",   189);
            seedLogs(rootUser, Departments.BCPC,      "BCPC",      156);
            seedLogs(rootUser, Departments.FTJS,      "FTJS",       98);
            seedLogs(rootUser, Departments.CLEARANCE, "CLEARANCE", 312);

            // FIX: Added .department() — column is nullable = false, this would crash before
            List<AuditLog> criticalLogs = new ArrayList<>();
            for (int i = 0; i < 3; i++) {
                criticalLogs.add(AuditLog.builder()
                        .user(rootUser)
                        .department(Departments.ROOT_ADMIN)
                        .severity(Severity.CRITICAL)
                        .module("SECURITY")
                        .actionTaken("UNAUTHORIZED_ACCESS_ATTEMPT")
                        .reason("Detected multiple failed attempts from unrecognized IP")
                        .ipAddress("192.168.1.50")
                        .build());
            }
            auditLogRepository.saveAll(criticalLogs);

            seedHistoricalLogs(rootUser, 50);

            System.out.println("Dashboard testing data seeded successfully.");
        }


    }






    // ── Private Helpers ───────────────────────────────────────────────────────

    // ── Private Helpers ───────────────────────────────────────────────────────

    /**
     * FIX: Was hardcoding Departments.ROOT_ADMIN — now uses the dept param correctly.
     */
    private void seedLogs(User actor, Departments dept, String module, int count) {
        List<AuditLog> logsToSave = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            logsToSave.add(AuditLog.builder()
                    .user(actor)
                    .department(dept)
                    .module(module)
                    .severity(Severity.INFO)
                    .actionTaken("CREATE_RECORD")
                    .reason("Initial migration data for " + module)
                    .ipAddress("127.0.0.1")
                    .build());
        }
        auditLogRepository.saveAll(logsToSave);
    }

    /**
     * Seeds historical logs with a backdated created_at timestamp.
     */
    private void seedHistoricalLogs(User actor, int count) {
        // Use ROOT_ADMIN as the department for historical/system-generated logs
        Departments dept = Departments.ROOT_ADMIN;
        LocalDateTime lastMonth = LocalDateTime.now().minusMonths(1).minusDays(5);

        String roleName = (actor.getRole() != null)
                ? actor.getRole().getRoleName()
                : "SYSTEM";

        List<AuditLog> logsToSave = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            logsToSave.add(AuditLog.builder()
                    .user(actor)
                    .department(dept)
                    .severity(Severity.INFO)
                    .module(roleName)
                    .actionTaken("HISTORICAL_LOG")
                    .reason("Generated log for " + roleName)
                    .ipAddress("127.0.0.1")
                    .build());
        }

        // Save all first, then batch-update created_at to backdate them
        List<AuditLog> savedLogs = auditLogRepository.saveAll(logsToSave);

        List<Object[]> batchArgs = savedLogs.stream()
                .map(log -> new Object[]{lastMonth, log.getId()})
                .collect(Collectors.toList());

        jdbcTemplate.batchUpdate(
                "UPDATE audit_logs SET created_at = ? WHERE id = ?",
                batchArgs
        );
    }

    private Department createDeptIfNotFound(String name) {
        return departmentRepository.findByName(name)
                .orElseGet(() -> {
                    Department dept = new Department();
                    dept.setName(name);
                    return departmentRepository.save(dept);
                });
    }

    private Permission createPermIfNotFound(String name) {
        return permissionRepository.findByPermissionName(name)
                .orElseGet(() -> {
                    Permission perm = new Permission();
                    perm.setPermissionName(name);
                    return permissionRepository.save(perm);
                });
    }

    private void createRoleIfNotFound(String name) {
        if (roleRepository.findByRoleName(name).isEmpty()) {
            Role role = new Role();
            role.setRoleName(name);
            roleRepository.save(role);
        }
    }

    private void createNatureIfNotFound(String name) {
        if (natureOfComplaintRepository.findByName(name).isEmpty()) {
            NatureOfComplaint nature = new NatureOfComplaint();
            nature.setName(name);
            natureOfComplaintRepository.save(nature);
        }
    }

    private void createRelationIfNotFound(String name) {
        if (relationshipTypeRepository.findByName(name).isEmpty()) {
            RelationshipType type = new RelationshipType();
            type.setName(name);
            relationshipTypeRepository.save(type);
        }
    }



    private void createFrequencyIfNotFound(String label) {
        if (incidentFrequencyRepository.findByLabel(label).isEmpty()) {
            IncidentFrequency freq = new IncidentFrequency();
            freq.setLabel(label);
            incidentFrequencyRepository.save(freq);
        }
    }

    private void createEvidenceTypeIfNotFound(String name) {
        if (evidenceTypeRepository.findByTypeName(name).isEmpty()) {
            EvidenceType type = new EvidenceType();
            type.setTypeName(name);
            evidenceTypeRepository.save(type);
        }
    }




    private void seedResidents(int count) {
        String[] firstNames = {"Juan", "Maria", "Jose", "Elena", "Ricardo", "Gloria", "Roberto", "Teresa", "Fernando", "Imelda", "Francisco", "Lourdes", "Antonio", "Corazon", "Jaime", "Remedios", "Benjamin", "Carmelita", "Rodolfo", "Dolores"};
        String[] lastNames = {"Dela Cruz", "Garcia", "Reyes", "Ramos", "Mendoza", "Santos", "Flores", "Gonzales", "Bautista", "Villanueva", "Fernandez", "Cruz", "Lopez", "Castillo", "Gomez", "Pineda", "Madronio", "De Leon", "Mercado", "Rivera"};
        String[] genders = {"Male", "Female"};
        String[] civilStatuses = {"Single", "Married", "Widowed", "Separated"};

        List<People> peopleList = new ArrayList<>();
        Random random = new Random();

        System.out.println("Seeding 100 Filipino Residents...");

        for (int i = 0; i < count; i++) {
            People person = new People();
            person.setFirstName(firstNames[random.nextInt(firstNames.length)]);
            person.setLastName(lastNames[random.nextInt(lastNames.length)]);
            person.setMiddleName(lastNames[random.nextInt(lastNames.length)]);
            person.setGender(genders[random.nextInt(genders.length)]);
            person.setCivilStatus(civilStatuses[random.nextInt(civilStatuses.length)]);
            person.setContactNumber("09" + (100000000 + random.nextInt(900000000)));
            person.setCompleteAddress("Bgy. Novaliches, Quezon City, Metro Manila");
            person.setIsResident(true);
            person.setEmail(person.getFirstName().toLowerCase() + i + "@example.com");

            // Logic para sa Age at BirthDate
            int age;
            if (i < 10) {
                // Siguradong 10 Senior Citizens (60-85 years old)
                age = 60 + random.nextInt(25);
            } else {
                // Regular adults (18-59 years old)
                age = 18 + random.nextInt(42);
            }
            person.setAge((short) age);
            person.setBirthDate(LocalDate.now().minusYears(age).minusDays(random.nextInt(365)));

            // I-save muna ang People (Master)
            People savedPerson = peopleRepository.save(person);

            // Gawa ng Resident Profile
            Resident resident = new Resident();
            resident.setPerson(savedPerson);
            resident.setBarangayIdNumber("BID-2026-" + String.format("%04d", i));
            resident.setHouseholdNumber("HH-" + (1000 + random.nextInt(9000)));
            resident.setPrecinctNumber("PR-" + (100 + random.nextInt(900)));

            // Logic: Lahat ng senior ay voter, 80% ng adults ay voter
            resident.setIsVoter(age >= 60 || random.nextDouble() < 0.8);

            // 25% chance na maging Head of Family
            resident.setIsHeadOfFamily(random.nextDouble() < 0.25);

            resident.setOccupation(age >= 60 ? "Retired" : "Employee");
            resident.setCitizenship("Filipino");
            resident.setDateOfResidency(LocalDate.now().minusYears(random.nextInt(10)));

            residentRepository.save(resident);
        }
        System.out.println("Successfully seeded 100 residents.");
    }






}



