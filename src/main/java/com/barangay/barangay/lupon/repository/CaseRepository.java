package com.barangay.barangay.lupon.repository;

import com.barangay.barangay.blotter.model.BlotterCase;
import com.barangay.barangay.enumerated.CaseStatus;
import com.barangay.barangay.lupon.dto.LuponSummaryDTO;
import com.barangay.barangay.lupon.dto.dashboard.CaseStatusDistributionDTO;
import com.barangay.barangay.lupon.dto.dashboard.DashboardStatsDTO;
import com.barangay.barangay.lupon.dto.dashboard.RecentCaseDTO;
import com.barangay.barangay.lupon.dto.reports.NatureReportDTO;
import com.barangay.barangay.lupon.dto.reports.ReportsStatsDTO;
import com.barangay.barangay.lupon.dto.reports.StatusStatDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface CaseRepository extends JpaRepository<BlotterCase,Long> {


    @Query("""
    SELECT new com.barangay.barangay.lupon.dto.LuponSummaryDTO(
        b.id,
        b.blotterNumber,
        CONCAT(c.person.firstName, ' ', c.person.lastName),
        CONCAT(r.person.firstName, ' ', r.person.lastName),
        n.name,
        b.dateFiled,
        CAST(b.status AS string)
    )
    FROM BlotterCase b
    JOIN b.complainant c
    JOIN b.respondent r
    JOIN b.incidentDetail id
    JOIN id.natureOfComplaint n
    WHERE b.department.name = :deptName
    AND b.status IN :statuses /* 👈 ITO YUNG DINAGDAG NA CONDITION PARA SA STATUS */
    AND (CAST(:natureId AS Long) IS NULL OR n.id = :natureId)
    AND (CAST(:startDate AS LocalDateTime) IS NULL OR b.dateFiled >= :startDate)
    AND (CAST(:endDate AS LocalDateTime) IS NULL OR b.dateFiled <= :endDate)
    AND (CAST(:search AS String) IS NULL OR (
        b.blotterNumber ILIKE %:search% OR
        c.person.firstName ILIKE %:search% OR
        c.person.lastName ILIKE %:search% OR
        r.person.firstName ILIKE %:search% OR
        r.person.lastName ILIKE %:search%
    ))
    ORDER BY b.dateFiled DESC
""")
    Page<LuponSummaryDTO> findLuponSummaryWithFilters(
            @Param("deptName") String deptName,
            @Param("statuses") List<CaseStatus> statuses,
            @Param("search") String search,
            @Param("natureId") Long natureId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            Pageable pageable);


    @Query("SELECT COUNT(bc) FROM BlotterCase bc WHERE bc.referredToLuponAt IS NOT NULL AND bc.department.id = :deptId")
    long countTotalReferred(@Param("deptId") Long deptId);

    @Query("SELECT COUNT(bc) FROM BlotterCase bc WHERE bc.referredToLuponAt IS NOT NULL AND bc.status = 'UNDER_CONCILIATION' AND bc.department.id = :deptId")
    long countActiveConciliation(@Param("deptId") Long deptId);

    @Query("SELECT COUNT(bc) FROM BlotterCase bc WHERE bc.referredToLuponAt IS NOT NULL AND bc.settledAt IS NOT NULL AND bc.department.id = :deptId")
    long countSettled(@Param("deptId") Long deptId);

    @Query("SELECT COUNT(bc) FROM BlotterCase bc WHERE bc.referredToLuponAt IS NOT NULL AND bc.status = 'CERTIFIED_TO_FILE_ACTION' AND bc.department.id = :deptId")
    long countCfaIssued(@Param("deptId") Long deptId);


    @EntityGraph(attributePaths = {
            "department", "receivingOfficer", "complainant.person",
            "respondent.person", "respondent.relationshipType",
            "incidentDetail.natureOfComplaint", "incidentDetail.frequency",
            "narrativeStatement", "witnesses", "witnesses.person"
    })
    Optional<BlotterCase> findByBlotterNumber(String blotterNumber);




    @Query("""
        SELECT COUNT(b.id) FROM BlotterCase b 
        WHERE b.department.name = :deptName 
        AND b.status = 'PENDING'
    """)
    Long countPendingCases(@Param("deptName") String deptName);

    @Query("""
        SELECT COUNT(b.id) FROM BlotterCase b 
        WHERE b.department.name = :deptName 
        AND b.status = 'UNDER_CONCILIATION' 
        AND b.luponDeadline BETWEEN :now AND :warningDate
    """)
    Long countCasesNearingDeadline(
            @Param("deptName") String deptName,
            @Param("now") LocalDateTime now,
            @Param("warningDate") LocalDateTime warningDate
    );

    @Query("""
        SELECT COUNT(b.id) FROM BlotterCase b 
        WHERE b.department.name = :deptName 
        AND b.status = 'SETTLED' 
        AND b.settledAt BETWEEN :startOfMonth AND :endOfMonth
    """)
    Long countSettledThisMonth(
            @Param("deptName") String deptName,
            @Param("startOfMonth") LocalDateTime startOfMonth,
            @Param("endOfMonth") LocalDateTime endOfMonth
    );


    @Query("""
        SELECT COUNT(b.id) FROM BlotterCase b 
        WHERE b.department.name = :deptName 
        AND b.dateFiled >= :startDate 
        AND b.dateFiled <= :endDate
    """)
    Long countCasesByMonthRange(
            @Param("deptName") String deptName,
            @Param("startDate") java.time.LocalDateTime startDate,
            @Param("endDate") java.time.LocalDateTime endDate
    );


    @Query("SELECT new com.barangay.barangay.lupon.dto.dashboard.CaseStatusDistributionDTO(bc.status, COUNT(bc)) " +
            "FROM BlotterCase bc " +
            "WHERE bc.department.name = :deptName " +
            "GROUP BY bc.status")
    List<CaseStatusDistributionDTO> getCaseStatusDistributionByDepartment(@Param("deptName") String deptName);



    @Query("SELECT new com.barangay.barangay.lupon.dto.dashboard.RecentCaseDTO(" +
            "bc.id, bc.blotterNumber, bc.caseType, " +
            "CONCAT(bc.complainant.person.firstName, ' ', bc.complainant.person.lastName), " +
            "CONCAT(bc.respondent.person.firstName, ' ', bc.respondent.person.lastName), " +
            "bc.status, bc.dateFiled) " +
            "FROM BlotterCase bc " +
            "WHERE bc.department.name = :deptName " +
            "ORDER BY bc.dateFiled DESC")
        List<RecentCaseDTO> findRecentCasesByDepartment(@Param("deptName") String deptName, Pageable pageable);






    @Query("""
    SELECT new com.barangay.barangay.lupon.dto.reports.ReportsStatsDTO(
        SUM(CASE WHEN bc.status = 'UNDER_CONCILIATION' THEN 1 ELSE 0 END),
        SUM(CASE WHEN bc.status = 'SETTLED' THEN 1 ELSE 0 END),
        SUM(CASE WHEN bc.status = 'CLOSED' THEN 1 ELSE 0 END),
        SUM(CASE WHEN bc.status = 'CERTIFIED_TO_FILE_ACTION' THEN 1 ELSE 0 END)
    )
    FROM BlotterCase bc
    WHERE bc.department.name = :deptName
    AND bc.dateFiled >= :startDate 
    AND bc.dateFiled <= :endDate
""")
    ReportsStatsDTO getLuponStats(
            @Param("deptName") String deptName,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );



    @Query("""
    SELECT new com.barangay.barangay.lupon.dto.reports.StatusStatDTO(
        CAST(bc.status AS string), 
        COUNT(bc)
    )
    FROM BlotterCase bc
    WHERE bc.department.name = 'LUPONG_TAGAPAMAYAPA'
    AND bc.dateFiled BETWEEN :startDate AND :endDate
    GROUP BY bc.status
""")
    List<StatusStatDTO> getStatusDistribution(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );


    @Query("""
        SELECT new com.barangay.barangay.lupon.dto.reports.NatureReportDTO(
            nc.name, 
            COUNT(bc)
        )
        FROM BlotterCase bc
        JOIN bc.incidentDetail id
        JOIN id.natureOfComplaint nc
        WHERE bc.department.name = 'LUPONG_TAGAPAMAYAPA'
        AND bc.dateFiled BETWEEN :startDate AND :endDate
        GROUP BY nc.name
        ORDER BY COUNT(bc) DESC
    """)
    List<NatureReportDTO> getTop5NatureByLupon(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            Pageable pageable
    );



    @Query("""
    SELECT CAST(bc.dateFiled AS LocalDate), COUNT(bc)
    FROM BlotterCase bc
    WHERE bc.department.name = 'LUPONG_TAGAPAMAYAPA'
    AND bc.dateFiled BETWEEN :start AND :end
    GROUP BY CAST(bc.dateFiled AS LocalDate)
    ORDER BY CAST(bc.dateFiled AS LocalDate) ASC
""")
    List<Object[]> getRawDailyCounts(
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );


    @Query("""
        SELECT DISTINCT bc FROM BlotterCase bc
        JOIN FETCH bc.department d
        JOIN FETCH bc.complainant c
        JOIN FETCH c.person cp
        JOIN FETCH bc.respondent r
        JOIN FETCH r.person rp
        LEFT JOIN FETCH bc.incidentDetail id
        LEFT JOIN FETCH id.natureOfComplaint nc
        WHERE d.name = :deptName
        AND bc.dateFiled BETWEEN :startDate AND :endDate
        ORDER BY bc.dateFiled ASC
    """)
    List<BlotterCase> findCasesByDateRange(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            @Param("deptName") String deptName
    );




}
