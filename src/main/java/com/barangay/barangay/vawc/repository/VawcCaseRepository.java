package com.barangay.barangay.vawc.repository;

import com.barangay.barangay.blotter.model.BlotterCase;
import com.barangay.barangay.enumerated.CaseStatus;
import com.barangay.barangay.vawc.model.BaranggayProtectionOrder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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





}
