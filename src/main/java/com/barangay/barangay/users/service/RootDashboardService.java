package com.barangay.barangay.users.service;

import com.barangay.barangay.audit.repository.AuditLogRepository;
import com.barangay.barangay.users.dto.ActivityOverviewDTO;
import com.barangay.barangay.users.dto.DashboardStats;
import com.barangay.barangay.users.dto.DeptActivityDTO;
import com.barangay.barangay.users.dto.RecentSystemAction;
import com.barangay.barangay.users.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RootDashboardService {

    private final UserRepository userRepository;
    private final AuditLogRepository auditLogRepository;


    public DashboardStats getDashboardStats() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime firstDayThisMonth = now.withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0);
        LocalDateTime firstDayLastMonth = firstDayThisMonth.minusMonths(1);


        Long totalUser = userRepository.countAllUsers();
        Long totalActive = userRepository.countActiveUsers();
        Long totalCritical = auditLogRepository.countCriticalAlerts();
        Long totalAudit = auditLogRepository.countAllLogs();


        Long currentMonth = auditLogRepository.countLogsThisMonth(firstDayThisMonth);
        Long lastMonth = auditLogRepository.countLogsLastMonth(firstDayLastMonth, firstDayThisMonth);

        Long growth = currentMonth - lastMonth;
        String direction = (growth > 0) ? "up" : (growth < 0) ? "down" : "neutral";

        return new DashboardStats(
                totalUser,
                totalActive,
                totalCritical,
                totalAudit,
                Math.abs(growth),
                direction
        );
    }


    @Transactional
    public ActivityOverviewDTO getActivityOverview() {
        LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);

        List<Object[]> rawData = auditLogRepository.countLogsByDepartment(thirtyDaysAgo);

        long totalCount = rawData.stream()
                .mapToLong(row -> (long) row[1])
                .sum();

        List<DeptActivityDTO> deptActivities = rawData.stream().map(row -> {
            String name = (String) row[0];
            long count = (long) row[1];

            BigDecimal percentage = totalCount > 0
                    ? BigDecimal.valueOf(count)
                    .multiply(BigDecimal.valueOf(100))
                    .divide(BigDecimal.valueOf(totalCount), 1, RoundingMode.HALF_UP)
                    : BigDecimal.ZERO;

            return new DeptActivityDTO(name, count, percentage);
        }).toList();

        return new ActivityOverviewDTO(totalCount, deptActivities);
    }



    @Transactional(readOnly = true)
    public List<RecentSystemAction> getRecentActions() {
        return auditLogRepository.findTop5RecentActions();
    }
}
