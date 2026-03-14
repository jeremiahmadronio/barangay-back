package com.barangay.barangay.blotter.repository;

import com.barangay.barangay.blotter.dto.reports.NatureStatDTO;
import com.barangay.barangay.blotter.dto.reports.StatusStatDTO;
import com.barangay.barangay.blotter.model.BlotterCase;
import com.barangay.barangay.department.model.Department;
import com.barangay.barangay.enumerated.CaseStatus;
import com.barangay.barangay.enumerated.CaseType;

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

    List<BlotterCase> findAllByStatusAndDateFiledBefore(CaseStatus status, LocalDateTime threshold);

    long countByDepartmentAndCreatedAtBetween(Department dept, LocalDateTime start, LocalDateTime end);

    long countByCaseTypeAndDepartmentAndCreatedAtBetween(CaseType type, Department dept, LocalDateTime start, LocalDateTime end);

    long countByStatusAndDepartmentAndCreatedAtBetween(CaseStatus status, Department dept, LocalDateTime start, LocalDateTime end);

    long countByDepartmentAndCaseType(Department dept, CaseType type);

    long countByDepartmentAndCaseTypeAndStatus(Department dept, CaseType type, CaseStatus status);




    // Bilangin lahat ng nagpa-record (FTR)
    @Query("""
        SELECT COUNT(bc) FROM BlotterCase bc 
        WHERE bc.department.id = :deptId 
          AND bc.caseType = 'FOR_THE_RECORD' 
          AND bc.createdAt >= :startDate AND bc.createdAt <= :endDate
    """)
    long countTotalFtr(@Param("deptId") Long deptId, @Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    @Query("""
        SELECT COUNT(bc) FROM BlotterCase bc 
        WHERE bc.department.id = :deptId 
          AND bc.caseType = 'FOR_THE_RECORD' 
          AND bc.status = 'ELEVATED_TO_FORMAL' 
          AND bc.createdAt >= :startDate AND bc.createdAt <= :endDate
    """)
    long countEscalatedFtr(@Param("deptId") Long deptId, @Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    @Query("""
        SELECT bc.incidentDetail.timeOfIncident 
        FROM BlotterCase bc 
        WHERE bc.department.id = :deptId 
          AND bc.caseType = 'FOR_THE_RECORD'
          AND bc.createdAt >= :startDate
          AND bc.incidentDetail.timeOfIncident IS NOT NULL
    """)
    List<LocalTime> findFtrIncidentTimesThisMonth(@Param("deptId") Long deptId, @Param("startDate") LocalDateTime startDate);

    @Query("""
        SELECT new com.barangay.barangay.blotter.dto.reports.NatureStatDTO(
            id.natureOfComplaint.name,
            COUNT(bc)
        )
        FROM BlotterCase bc
        JOIN bc.incidentDetail id
        WHERE bc.department.id = :deptId
        GROUP BY id.natureOfComplaint.name
    """)
    List<NatureStatDTO> countCasesByNature(@Param("deptId") Long deptId);

    @Query("""
        SELECT new com.barangay.barangay.blotter.dto.reports.StatusStatDTO(
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
}
