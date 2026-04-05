package com.barangay.barangay.blotter.repository;

import com.barangay.barangay.blotter.model.BlotterCase;
import com.barangay.barangay.department.model.Department;
import com.barangay.barangay.enumerated.CaseStatus;
import com.barangay.barangay.enumerated.CaseType;

import com.barangay.barangay.lupon.dto.dashboard.CaseStatusDistributionDTO;
import com.barangay.barangay.lupon.dto.dashboard.RecentCaseDTO;
import com.barangay.barangay.person.model.Person;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
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



    @Query("""
    SELECT COUNT(bc) FROM BlotterCase bc 
    JOIN bc.luponReferral lr 
    WHERE lr.referredAt BETWEEN :start AND :end 
    AND (bc.department.id = :deptId OR :deptId = 3)
""")
    long countAllReferredToLupon(
            @Param("deptId") Long deptId,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );

    List<BlotterCase> findAllByStatusAndDepartmentIsNullAndDateFiledBefore(
            CaseStatus status, LocalDateTime threshold);

    List<BlotterCase> findAllByStatusAndDepartmentNameAndLuponReferral_DeadlineBefore(
            CaseStatus status,
            String deptName,
            LocalDateTime deadline
    );



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
    SELECT id.natureOfComplaint, COUNT(bc)
    FROM BlotterCase bc
    JOIN bc.incidentDetail id
    WHERE bc.department.name IN ('BLOTTER', 'LUPONG_TAGAPAMAYAPA')
    AND bc.dateFiled BETWEEN :start AND :end
    GROUP BY id.natureOfComplaint
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










    @Query(value = """
    SELECT COUNT(*) FROM ( 
        SELECT r.person_id 
        FROM cases bc 
        JOIN respondents r ON bc.respondent_id = r.id 
        WHERE bc.dept_id = :deptId 
          AND bc.case_type = 'FOR_THE_RECORD' 
          AND bc.case_filed_at >= :start 
        GROUP BY r.person_id 
        HAVING COUNT(*) >= 2
    ) AS suki
    """, nativeQuery = true)
    long countFrequentFtrSubjects(@Param("deptId") Long deptId, @Param("start") LocalDateTime start);

    // 2. Most Reported Issue (Nature of Complaint)
    @Query(value = """
    SELECT id.nature_of_complaint 
    FROM cases bc 
    JOIN incident_details id ON bc.incident_detail_id = id.id 
    WHERE bc.dept_id = :deptId 
      AND bc.case_type = 'FOR_THE_RECORD' 
      AND bc.case_filed_at >= :since 
    GROUP BY id.nature_of_complaint 
    ORDER BY COUNT(bc.id) DESC 
    LIMIT 1
    """, nativeQuery = true)
    Optional<String> findTopFtrNature(@Param("deptId") Long deptId, @Param("since") LocalDateTime since);
    @Query(value = """
    SELECT id.incident_time 
    FROM cases bc 
    JOIN incident_details id ON bc.incident_detail_id = id.id 
    WHERE bc.dept_id = :deptId 
      AND bc.case_type = 'FOR_THE_RECORD' 
      AND bc.case_filed_at >= :since 
      AND id.incident_time IS NOT NULL
    """, nativeQuery = true)
    List<java.sql.Time> findFtrIncidentTimesRaw(@Param("deptId") Long deptId, @Param("since") LocalDateTime since);

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
    JOIN b.luponReferral lr
    WHERE b.department.name IN :deptNames 
    AND (b.status = 'UNDER_MEDIATION' OR b.status = 'UNDER_CONCILIATION') 
    AND lr.deadline BETWEEN :now AND :warningDate
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


    List<BlotterCase> findAllByComplainant_Person(Person person);

    List<BlotterCase> findAllByRespondent_Person(Person person);

    @Query("SELECT bc FROM BlotterCase bc JOIN bc.witnesses w WHERE w.person = :person")
    List<BlotterCase> findAllByWitnessPerson(@Param("person") Person person);
}
