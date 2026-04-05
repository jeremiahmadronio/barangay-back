package com.barangay.barangay.vawc.repository;

import com.barangay.barangay.vawc.model.BaranggayProtectionOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BarangayProtectionOrderRepository extends JpaRepository<BaranggayProtectionOrder, Long> {

    Optional<BaranggayProtectionOrder> findByBlotterCaseId(Long caseId);

    @Query("""
        SELECT bpo FROM BaranggayProtectionOrder bpo
        JOIN FETCH bpo.blotterCase bc
        JOIN FETCH bc.complainant c JOIN FETCH c.person cp
        JOIN FETCH bc.respondent r JOIN FETCH r.person rp
        LEFT JOIN FETCH bc.employee e LEFT JOIN FETCH e.person ep
        WHERE bc.id = :caseId
    """)
    Optional<BaranggayProtectionOrder> findBpoDetailsByCaseId(@Param("caseId") Long caseId);

    boolean existsByBpoControlNumber(String bpoControlNumber);

}
