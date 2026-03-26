package com.barangay.barangay.blotter.repository;

import com.barangay.barangay.blotter.dto.reports_and_display.NatureStatDTO;
import com.barangay.barangay.blotter.dto.reports_and_display.StatusStatDTO;
import com.barangay.barangay.blotter.model.BlotterCase;
import com.barangay.barangay.department.model.Department;
import com.barangay.barangay.enumerated.CaseStatus;
import com.barangay.barangay.enumerated.CaseType;

import com.barangay.barangay.lupon.dto.dashboard.CaseStatusDistributionDTO;
import com.barangay.barangay.lupon.dto.dashboard.RecentCaseDTO;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface BlotterCaseRepository extends JpaRepository<BlotterCase, Long>, JpaSpecificationExecutor<BlotterCase> {

    Optional<BlotterCase> findByBlotterNumber(String blotterNumber);


    boolean existsByBlotterNumber(String blotterNumber);



    long countByCaseTypeAndDepartment(CaseType caseType, Department department);

    long countByCaseTypeAndStatusInAndDepartment(CaseType caseType, Collection<CaseStatus> statuses, Department department);

    long countByCaseTypeAndStatusAndDepartment(CaseType caseType, CaseStatus status, Department department);


    long countByDepartmentAndDateFiledBetween(Department dept, LocalDateTime start, LocalDateTime end);
    long countByCaseTypeAndDepartmentAndDateFiledBetween(CaseType type, Department dept, LocalDateTime start, LocalDateTime end);



    @Query("SELECT COUNT(bc) FROM BlotterCase bc " +
            "WHERE bc.referredToLuponAt BETWEEN :start AND :end " +
            "AND (bc.department.id = :deptId OR :deptId = 3)") // Kung taga-Blotter (3), ipakita lahat ng na-refer
    long countAllReferredToLupon(@Param("deptId") Long deptId,
                                 @Param("start") LocalDateTime start,
                                 @Param("end") LocalDateTime end);

    List<BlotterCase> findAllByStatusAndDepartmentIsNullAndDateFiledBefore(
            CaseStatus status, LocalDateTime threshold);

    List<BlotterCase> findAllByStatusAndDepartmentNameAndLuponDeadlineBefore(
            CaseStatus status, String deptName, LocalDateTime now);



    @Query("""
    SELECT CAST(bc.dateFiled AS LocalDate), COUNT(bc)
    FROM BlotterCase bc
WHERE bc.department.name IN ('BLOTTER', 'LUPONG_TAGAPAMAYAPA')
    AND bc.dateFiled BETWEEN :start AND :end
    GROUP BY CAST(bc.dateFiled AS LocalDate)
    ORDER BY CAST(bc.dateFiled AS LocalDate) ASC
""")
    List<Object[]> getRawDailyCounts(
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );


    @Query("""
    SELECT id.natureOfComplaint.name, COUNT(bc)
    FROM BlotterCase bc
    JOIN bc.incidentDetail id
    WHERE bc.department.name IN ('BLOTTER', 'LUPONG_TAGAPAMAYAPA')
    AND bc.dateFiled BETWEEN :start AND :end
    GROUP BY id.natureOfComplaint.name
    ORDER BY COUNT(bc) DESC
""")
    List<Object[]> countCasesByNatureFiltered(
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );


    @Query("""
    SELECT bc.status, COUNT(bc)
    FROM BlotterCase bc
    WHERE bc.department.name IN ('BLOTTER', 'LUPONG_TAGAPAMAYAPA')
    AND bc.dateFiled BETWEEN :start AND :end
    GROUP BY bc.status
""")
    List<Object[]> countCasesByStatusFiltered(
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );



    @Query("""
        SELECT COUNT(bc) FROM BlotterCase bc 
        WHERE bc.department.name IN ('BLOTTER', 'LUPONG_TAGAPAMAYAPA')
        AND bc.caseType = 'FORMAL_COMPLAINT'
        AND bc.dateFiled BETWEEN :start AND :end
    """)
    long countTotalFormalFiltered(LocalDateTime start, LocalDateTime end);

    @Query("""
        SELECT COUNT(bc) FROM BlotterCase bc 
        WHERE bc.department.name IN ('BLOTTER', 'LUPONG_TAGAPAMAYAPA')
        AND bc.caseType = 'FORMAL_COMPLAINT'
        AND bc.status = 'SETTLED'
        AND bc.dateFiled BETWEEN :start AND :end
    """)
    long countSettledFormalFiltered(LocalDateTime start, LocalDateTime end);






    @Query("""
        SELECT new com.barangay.barangay.blotter.dto.reports_and_display.StatusStatDTO(
            CAST(bc.status AS string),
            COUNT(bc)
        )
        FROM BlotterCase bc
        WHERE bc.department.id = :deptId
        GROUP BY bc.status
    """)
    List<StatusStatDTO> countCasesByStatus(@Param("deptId") Long deptId);


    @Query(value = """
        SELECT 
            to_char(date_trunc('month', created_at), 'Mon') AS month_label, 
            COUNT(*) AS total_count
        FROM blotter_cases
        WHERE dept_id = :deptId 
          AND created_at >= date_trunc('month', CURRENT_DATE) - INTERVAL '4 months'
        GROUP BY date_trunc('month', created_at)
        ORDER BY date_trunc('month', created_at) ASC
        """, nativeQuery = true)
    List<Object[]> findMonthlyTrendsNative(@Param("deptId") Long deptId);



    @Query(value = "SELECT COUNT(*) FROM ( " +
            "SELECT r.person_id FROM respondents r " +
            "JOIN blotter_cases bc ON r.case_id = bc.id " +
            "WHERE bc.dept_id = :deptId " + // Isang ID na lang
            "AND bc.case_type = 'FOR_THE_RECORD' " +
            "AND bc.date_filed >= :start " +
            "GROUP BY r.person_id HAVING COUNT(*) >= 2) AS suki",
            nativeQuery = true)
    long countFrequentFtrSubjects(@Param("deptId") Long deptId, @Param("start") LocalDateTime start);

    // 2. Most Reported Issue (Nature of Complaint)
    @Query(value = "SELECT n.name FROM blotter_cases bc " +
            "JOIN incident_details id ON bc.id = id.case_id " +
            "JOIN nature_of_complaints n ON id.nature_of_complaint_id = n.id " +
            "WHERE bc.dept_id = :deptId " +
            "AND bc.case_type = 'FOR_THE_RECORD' " +
            "AND bc.date_filed >= :start " +
            "GROUP BY n.name ORDER BY COUNT(bc.id) DESC LIMIT 1",
            nativeQuery = true)
    Optional<String> findTopFtrNature(@Param("deptId") Long deptId, @Param("start") LocalDateTime start);

    // 3. Incident Times for Peak Time Card
    @Query(value = "SELECT id.time_of_incident FROM blotter_cases bc " +
            "JOIN incident_details id ON bc.id = id.case_id " +
            "WHERE bc.dept_id = :deptId " +
            "AND bc.case_type = 'FOR_THE_RECORD' " +
            "AND bc.date_filed >= :start " +
            "AND id.time_of_incident IS NOT NULL",
            nativeQuery = true)
    List<java.sql.Time> findFtrIncidentTimesRaw(@Param("deptId") Long deptId, @Param("start") LocalDateTime start);

    // 4. Total FTR Count
    @Query("SELECT COUNT(bc) FROM BlotterCase bc WHERE bc.department.id = :deptId " +
            "AND bc.caseType = :type AND bc.dateFiled BETWEEN :start AND :end")
    long countFtrByType(@Param("deptId") Long deptId, @Param("type") CaseType type,
                        @Param("start") LocalDateTime start, @Param("end") LocalDateTime end);







    @Query("""
    SELECT COUNT(b.id) FROM BlotterCase b 
    WHERE b.department.name IN :deptNames 
    AND b.status = 'PENDING'
""")
    Long countPendingCases(@Param("deptNames") List<String> deptNames);

    @Query("""
    SELECT COUNT(b.id) FROM BlotterCase b 
    WHERE b.department.name IN :deptNames 
    AND (b.status = 'UNDER_MEDIATION' OR b.status = 'UNDER_CONCILIATION') 
    AND b.luponDeadline BETWEEN :now AND :warningDate
""")
    Long countCasesNearingDeadline(
            @Param("deptNames") List<String> deptNames,
            @Param("now") LocalDateTime now,
            @Param("warningDate") LocalDateTime warningDate
    );

    @Query("""
    SELECT COUNT(b.id) FROM BlotterCase b 
    WHERE b.department.name IN :deptNames 
    AND b.status = 'SETTLED' 
    AND b.settledAt BETWEEN :startOfMonth AND :endOfMonth
""")
    Long countSettledThisMonth(
            @Param("deptNames") List<String> deptNames,
            @Param("startOfMonth") LocalDateTime startOfMonth,
            @Param("endOfMonth") LocalDateTime endOfMonth
    );

    @Query("SELECT new com.barangay.barangay.lupon.dto.dashboard.CaseStatusDistributionDTO(bc.status, COUNT(bc)) " +
            "FROM BlotterCase bc " +
            "WHERE bc.department.name IN :deptNames " +
            "GROUP BY bc.status")
    List<CaseStatusDistributionDTO> getCaseStatusDistributionByDepartments(@Param("deptNames") List<String> deptNames);

    @Query("SELECT new com.barangay.barangay.lupon.dto.dashboard.RecentCaseDTO(" +
            "bc.id, bc.blotterNumber, bc.caseType, " +
            "CONCAT(bc.complainant.person.firstName, ' ', bc.complainant.person.lastName), " +
            "CONCAT(bc.respondent.person.firstName, ' ', bc.respondent.person.lastName), " +
            "bc.status, bc.dateFiled) " +
            "FROM BlotterCase bc " +
            "WHERE bc.department.name IN :deptNames " +
            "ORDER BY bc.dateFiled DESC")
    List<RecentCaseDTO> findRecentCasesByDepartments(@Param("deptNames") List<String> deptNames, Pageable pageable);


    @Query("""
        SELECT COUNT(b.id) FROM BlotterCase b 
        WHERE b.department.name IN :deptNames 
        AND b.dateFiled >= :startDate 
        AND b.dateFiled <= :endDate
    """)
    Long countCasesByMonthRange(
            @Param("deptNames") List<String> deptNames,
            @Param("startDate") java.time.LocalDateTime startDate,
            @Param("endDate") java.time.LocalDateTime endDate
    );
}
