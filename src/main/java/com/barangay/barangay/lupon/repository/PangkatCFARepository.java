package com.barangay.barangay.lupon.repository;

import com.barangay.barangay.blotter.model.BlotterCase;
import com.barangay.barangay.lupon.model.PangkatCFA;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository

public interface PangkatCFARepository extends JpaRepository<PangkatCFA, Long> {

    Optional<PangkatCFA> findByBlotterCaseId(Long id);

    Optional<PangkatCFA> findByBlotterCase_BlotterNumber(String BlotterNumber);

    @Query("SELECT p FROM PangkatCFA p " +
            "JOIN FETCH p.blotterCase b " +
            "LEFT JOIN FETCH b.complainant c " +
            "LEFT JOIN FETCH b.respondent r " +
            "LEFT JOIN FETCH b.employee e " +
            "WHERE b.id = :caseId")
    Optional<PangkatCFA> findByBlotterCase(@Param("caseId") Long caseId);
}
