package com.barangay.barangay.lupon.repository;

import com.barangay.barangay.blotter.model.BlotterCase;
import com.barangay.barangay.lupon.model.PangkatCFA;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository

public interface PangkatCFARepository extends JpaRepository<PangkatCFA, Long> {

    Optional<PangkatCFA> findByBlotterCaseId(Long id);

    Optional<PangkatCFA> findByBlotterCase_BlotterNumber(String BlotterNumber);
}
