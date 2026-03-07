package com.barangay.barangay.audit.repository;

import com.barangay.barangay.audit.model.AuditLog;
import com.barangay.barangay.admin_management.dto.RecentSystemAction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog,Long>  {


    @Query("SELECT COUNT(a) FROM AuditLog a")
    Long countAllLogs();

    @Query("SELECT DISTINCT a.module FROM AuditLog a WHERE a.module IS NOT NULL ORDER BY a.module")
    List<String> findDistinctModules();

    @Query("SELECT DISTINCT a.actionTaken FROM AuditLog a WHERE a.actionTaken IS NOT NULL ORDER BY a.actionTaken")
    List<String> findDistinctActions();

    @Query("SELECT DISTINCT CAST(a.severity AS string) FROM AuditLog a WHERE a.severity IS NOT NULL ORDER BY CAST(a.severity AS string)")
    List<String> findDistinctSeverities();

    @Query("SELECT COUNT(a) FROM AuditLog a WHERE a.createdAt >= :startOfDay")
    Long countLogsToday(@Param("startOfDay") LocalDateTime startOfDay);

    @Query("SELECT COUNT(a) FROM AuditLog a WHERE a.severity = com.barangay.barangay.enumerated.Severity.WARNING")
    Long countWarningAlerts();

    @Query("SELECT COUNT(a) FROM AuditLog a WHERE a.severity = com.barangay.barangay.enumerated.Severity.CRITICAL")
    Long countCriticalAlerts();

    // Para sa growth computation
    @Query("SELECT COUNT(a) FROM AuditLog a WHERE a.createdAt >= :startOfMonth")
    Long countLogsThisMonth(@Param("startOfMonth") LocalDateTime startOfMonth);

    @Query("SELECT COUNT(a) FROM AuditLog a WHERE a.createdAt >= :startOfLastMonth AND a.createdAt < :startOfMonth")
    Long countLogsLastMonth(@Param("startOfLastMonth") LocalDateTime lastMonth, @Param("startOfMonth") LocalDateTime thisMonth);


    @Query("SELECT l.department, COUNT(l) FROM AuditLog l WHERE l.createdAt >= :startDate AND l.department IS NOT NULL GROUP BY l.department")
    List<Object[]> countLogsByDepartment(@Param("startDate") LocalDateTime startDate);

    @Query("""
            SELECT a FROM AuditLog a
            JOIN FETCH a.user u
            LEFT JOIN FETCH u.role
            WHERE a.id = :id
            """)
    Optional<AuditLog> findByIdWithDetails(@Param("id") Long id);


    @Query("""
        SELECT new com.barangay.barangay.admin_management.dto.RecentSystemAction(
            a.user.firstName,
            a.user.lastName,
            CAST(a.severity AS string),
            a.actionTaken,
            a.module,
            a.createdAt
        )
        FROM AuditLog a
        ORDER BY a.createdAt DESC
        LIMIT 5
    """)
    List<RecentSystemAction> findTop5RecentActions();


    @Query("""
    SELECT a FROM AuditLog a
    LEFT JOIN a.user u
    WHERE (:search IS NULL OR
              LOWER(u.firstName)  LIKE :search OR
              LOWER(u.lastName)   LIKE :search OR
              LOWER(a.reason)     LIKE :search OR
              LOWER(a.ipAddress)  LIKE :search)
      AND (:severity IS NULL OR CAST(a.severity AS string) = :severity)
      AND (:module   IS NULL OR a.module      = :module)
      AND (:action   IS NULL OR a.actionTaken = :action)
      AND (CAST(:startDate AS localdatetime) IS NULL OR a.createdAt >= :startDate)
      AND (CAST(:endDate AS localdatetime)   IS NULL OR a.createdAt <= :endDate)
    """)
    Page<AuditLog> findAllFiltered(
            @Param("search")    String search,
            @Param("severity")  String severity,
            @Param("module")    String module,
            @Param("action")    String action,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate")   LocalDateTime endDate,
            Pageable pageable
    );
}



