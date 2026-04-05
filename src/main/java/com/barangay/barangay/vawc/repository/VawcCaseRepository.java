package com.barangay.barangay.vawc.repository;

import com.barangay.barangay.blotter.model.BlotterCase;
import com.barangay.barangay.enumerated.CaseStatus;
import com.barangay.barangay.vawc.dto.projection.CategorySummaryProjection;
import com.barangay.barangay.vawc.dto.projection.NatureStatsProjection;
import com.barangay.barangay.vawc.dto.projection.ReportStatsProjection;
import com.barangay.barangay.vawc.dto.projection.TrendStatsProjection;
import com.barangay.barangay.vawc.model.BaranggayProtectionOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface VawcCaseRepository extends JpaRepository<BlotterCase, Long>, JpaSpecificationExecutor<BlotterCase> {

    @Query("SELECT COUNT(b) FROM BlotterCase b WHERE b.department.name = 'VAWC'")
    long countTotalVawc();

    @Query("SELECT COUNT(b) FROM BlotterCase b WHERE b.department.name = 'VAWC' AND b.status = :status")
    long countByStatus(@Param("status") CaseStatus status);

    @Query("""
        SELECT COUNT(b) FROM BlotterCase b 
        WHERE b.department.name = 'VAWC' 
            AND b.status IN (
                    com.barangay.barangay.enumerated.CaseStatus.PENDING,\s
                    com.barangay.barangay.enumerated.CaseStatus.UNDER_MEDIATION,\s
                    com.barangay.barangay.enumerated.CaseStatus.UNDER_CONCILIATION
                )
        AND b.dateFiled >= :startDate AND b.dateFiled <= :endDate
    """)
    long countExpiringBPOs(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);


    @Query("""
        SELECT b FROM BlotterCase b
        JOIN FETCH b.complainant c JOIN FETCH c.person cp
        JOIN FETCH b.respondent r JOIN FETCH r.person rp
        LEFT JOIN FETCH b.incidentDetail idet
        LEFT JOIN FETCH b.narrativeStatement ns
        LEFT JOIN FETCH b.employee e LEFT JOIN FETCH e.person ep
        LEFT JOIN FETCH b.createdBy u LEFT JOIN FETCH u.person up
        LEFT JOIN FETCH b.witnesses w LEFT JOIN FETCH w.person wp
        WHERE b.id = :id
    """)
    Optional<BlotterCase> findByIdWithFullDetails(@Param("id") Long id);

    @Query("SELECT bpo FROM BaranggayProtectionOrder bpo LEFT JOIN FETCH bpo.violenceTypes WHERE bpo.blotterCase.id = :caseId")
    Optional<BaranggayProtectionOrder> findBpoWithViolenceTypes(@Param("caseId") Long caseId);

    @Query("SELECT er.type.typeName FROM EvidenceRecord er WHERE er.blotterCase.id = :caseId")
    List<String> findEvidenceNamesByCaseId(@Param("caseId") Long caseId);

    Optional<BlotterCase> findByBlotterNumber(String blotterNumber);




    @Query(value = """
    SELECT 
        COUNT(DISTINCT bc.id) as totalCases,
        SUM(CASE WHEN bpo.status = 'EXPIRED' THEN 1 ELSE 0 END) as totalExpired,
        SUM(CASE WHEN bc.case_status IN ('RESOLVED', 'SETTLED', 'CERTIFIED_TO_FILE_ACTION','CLOSED') THEN 1 ELSE 0 END) as resolvedCases,
        COALESCE(
            ROUND(CAST(AVG(EXTRACT(EPOCH FROM (bc.settled_at - bc.case_filed_at)) / 86400.0) AS NUMERIC), 2), 
            0.0
        ) as avgResolutionTime
    FROM cases bc
    INNER JOIN departments d ON bc.dept_id = d.id
    LEFT JOIN baranggay_protection_order bpo ON bpo.case_id = bc.id
    WHERE d.name = 'VAWC'
    AND bc.case_filed_at >= :startDate 
    AND bc.case_filed_at <= :endDate
""", nativeQuery = true)
    ReportStatsProjection getVawcStats(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );


    @Query(value = """
    SELECT 
        TRIM(split_nature) as nature, 
        COUNT(*) as count
    FROM cases bc
    JOIN incident_details id ON bc.incident_detail_id = id.id
    JOIN departments d ON bc.dept_id = d.id,
    LATERAL regexp_split_to_table(id.nature_of_complaint, ',') AS split_nature
    WHERE d.name = 'VAWC'
    AND bc.case_filed_at >= :startDate 
    AND bc.case_filed_at <= :endDate
    GROUP BY TRIM(split_nature)
    ORDER BY count DESC
""", nativeQuery = true)
    List<NatureStatsProjection> getNatureOfComplaintStats(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );



    @Query(value = """
    SELECT 
        TRIM(TO_CHAR(bc.case_filed_at, 'Month')) as label, 
        COUNT(bc.id) as count
    FROM cases bc
    JOIN departments d ON bc.dept_id = d.id
    WHERE d.name = 'VAWC'
    AND bc.case_filed_at >= :startDate AND bc.case_filed_at <= :endDate
    GROUP BY label, EXTRACT(MONTH FROM bc.case_filed_at)
    ORDER BY EXTRACT(MONTH FROM bc.case_filed_at)
""", nativeQuery = true)
    List<TrendStatsProjection> getVawcMonthlyTrend(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    @Query(value = """
    SELECT 
        TO_CHAR(bc.case_filed_at, 'YYYY-MM-DD') as label, 
        COUNT(bc.id) as count
    FROM cases bc
    JOIN departments d ON bc.dept_id = d.id
    WHERE d.name = 'VAWC'
    AND bc.case_filed_at >= :startDate AND bc.case_filed_at <= :endDate
    GROUP BY label
    ORDER BY label
""", nativeQuery = true)
    List<TrendStatsProjection> getVawcDailyTrend(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);



    @Query(value = """
    WITH split_data AS (
        SELECT 
            TRIM(s.val) as cat_name,
            bc.id as case_id,
            bc.case_status,
            bpo.status as bpo_status
        FROM cases bc
        JOIN incident_details id ON bc.incident_detail_id = id.id
        JOIN departments d ON bc.dept_id = d.id
        LEFT JOIN baranggay_protection_order bpo ON bpo.case_id = bc.id
        CROSS JOIN LATERAL regexp_split_to_table(id.nature_of_complaint, ',') AS s(val)
        WHERE d.name = 'VAWC'
        AND bc.case_filed_at >= :start AND bc.case_filed_at <= :end
    )
    SELECT 
        cat_name as category,
        COUNT(case_id) as totalCases,
        SUM(CASE WHEN bpo_status = 'ISSUED' AND case_status NOT IN ('RESOLVED', 'SETTLED', 'REFERRED','WITHDRAWN') THEN 1 ELSE 0 END) as active,
        SUM(CASE WHEN case_status IN ('RESOLVED', 'SETTLED', 'REFERRED') THEN 1 ELSE 0 END) as resolved,
        SUM(CASE WHEN case_status NOT IN ('RESOLVED', 'SETTLED', 'REFERRED') AND (bpo_status IS NULL OR bpo_status != 'ISSUED') THEN 1 ELSE 0 END) as pending,
        ROUND(CAST(COUNT(case_id) * 100.0 / NULLIF(SUM(COUNT(case_id)) OVER(), 0) AS NUMERIC), 2) as percentage
    FROM split_data
    GROUP BY cat_name
    ORDER BY totalCases DESC
""", nativeQuery = true)
    List<CategorySummaryProjection> getDetailedCategoryReport(
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );


    @Query("SELECT b FROM BlotterCase b WHERE b.department.name = :deptName " +
            "AND b.status = :status AND b.dateFiled <= :deadline")
    List<BlotterCase> findPendingVawcToExpire(
            @Param("deptName") String deptName,
            @Param("status") CaseStatus status,
            @Param("deadline") LocalDateTime deadline);
}
