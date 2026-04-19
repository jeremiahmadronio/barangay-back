package com.barangay.barangay.user_management.service;

import com.barangay.barangay.admin_management.model.User;
import com.barangay.barangay.audit.repository.AuditLogRepository;
import com.barangay.barangay.department.model.Department;
import com.barangay.barangay.employee.repository.EmployeeRepository;
import com.barangay.barangay.enumerated.Departments;
import com.barangay.barangay.person.repository.ResidentRepository;
import com.barangay.barangay.user_management.dto.*;
import com.barangay.barangay.user_management.repository.UserManagementRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminDashboardService {

    private final EmployeeRepository  employeeRepository;
    private final UserManagementRepository userManagementRepository;
    private final AuditLogRepository auditLogRepository;
    private final ResidentRepository residentRepository;


        @Transactional(readOnly = true)
        public AdminDashboardStats getAdminStats(User currentAdmin) {
            LocalDateTime startOfToday = LocalDateTime.now().truncatedTo(ChronoUnit.DAYS);

            Set<Long> allowedDeptIds = currentAdmin.getAllowedDepartments().stream()
                    .map(Department::getId)
                    .collect(Collectors.toSet());


            List<String> excludedRoles = List.of("ROOT_ADMIN", "ADMIN");
            UUID currentUserId = currentAdmin.getId();

            List<String> excludedDepts = List.of("ADMINISTRATION", "ROOT_ADMIN");

            return new AdminDashboardStats(
                    residentRepository.count(),
                        userManagementRepository.countUsersExcludingAdminDepts(excludedRoles),
                    allowedDeptIds.isEmpty() ? 0L : employeeRepository.countByDepartmentIdIn(allowedDeptIds),
                    auditLogRepository.countByCreatedAtAfter(startOfToday)
            );
        }



    @Transactional(readOnly = true)
    public List<AdminDashboardOfficerByDepartmentDTO> countEmployeeByDepartment() {
        List<AdminDashboardOfficerByDepartmentDTO> stats = employeeRepository.countActiveByDepartment();
        Long inactiveCount = employeeRepository.countAllInactive();
        stats.add(new AdminDashboardOfficerByDepartmentDTO("Inactive", inactiveCount));
        return stats;
    }


    @Transactional(readOnly = true)
    public List<AdminDashboaradResidentByStatusDTO> getResidentDashboardStats() {
        List<AdminDashboaradResidentByStatusDTO> rawStats = residentRepository.getResidentStatusCounts();

        return rawStats.stream().map(stat -> {
            String formattedLabel = stat.statusLabel().replace("_", " ").toLowerCase();
            formattedLabel = formattedLabel.substring(0, 1).toUpperCase() + formattedLabel.substring(1);

            return new AdminDashboaradResidentByStatusDTO(formattedLabel, stat.count());
        }).collect(Collectors.toList());
    }


    @Transactional(readOnly = true)
    public List<AdminDashboardRecentAddedResidentDTO> getRecentResidents() {
        return residentRepository.findTop5ByOrderByCreatedDateDesc().stream()
                .map(r -> new AdminDashboardRecentAddedResidentDTO(
                        r.getBarangayIdNumber(),
                        r.getPerson().getFirstName() + " " + r.getPerson().getLastName(),
                        r.getIsVoter(),
                        r.getStatus().name()
                ))
                .collect(Collectors.toList());
    }



    @Transactional(readOnly = true)
    public List<AdminDashboardRecentActivityDTO> getAdminSpecificActivityFeed() {
        LocalDateTime startOfToday = LocalDateTime.now().truncatedTo(ChronoUnit.DAYS);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("h:mm a");

        Departments adminDept = Departments.ADMINISTRATION;

        return auditLogRepository.findTop5ByDepartmentAndCreatedAtAfterOrderByCreatedAtDesc(
                adminDept,
                startOfToday
        ).stream().map(log -> new AdminDashboardRecentActivityDTO(
                log.getActionTaken(),
                log.getCreatedAt().format(formatter)
        )).collect(Collectors.toList());
    }



    @Transactional(readOnly = true)
    public Map<String, Object> getMonthlyActivityOverview(User currentAdmin) {
        // AUTOMATIC START OF MONTH example April 1 -> April 30, 00:00:00
        LocalDateTime startOfMonth = LocalDateTime.now()
                .with(TemporalAdjusters.firstDayOfMonth())
                .truncatedTo(ChronoUnit.DAYS);

        Set<Departments> allowedDepts = currentAdmin.getAllowedDepartments().stream()
                .map(d -> Departments.valueOf(d.getName().toUpperCase()))
                .collect(Collectors.toSet());

        if (allowedDepts.isEmpty()) return Collections.emptyMap();

        List<Object[]> rawData = auditLogRepository.getActivityCountsByDept(allowedDepts, startOfMonth);

        long grandTotal = rawData.stream()
                .mapToLong(row -> (Long) row[1])
                .sum();
        List<AdminDashboardActivityByDepartment> stats = rawData.stream().map(row -> {
            String deptName = row[0].toString();
            long count = (Long) row[1];
            double percent = (grandTotal > 0) ? (count * 100.0 / grandTotal) : 0.0;

            return new AdminDashboardActivityByDepartment(
                    deptName,
                    count,
                    Math.round(percent * 10.0) / 10.0
            );
        }).collect(Collectors.toList());

        Map<String, Object> response = new HashMap<>();
        response.put("totalOverall", grandTotal);
        response.put("breakdown", stats);

        return response;
    }

}
