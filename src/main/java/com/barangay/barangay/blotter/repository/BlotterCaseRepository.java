package com.barangay.barangay.blotter.repository;

import com.barangay.barangay.blotter.model.BlotterCase;
import com.barangay.barangay.enumerated.CaseStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface BlotterCaseRepository extends JpaRepository<BlotterCase, Long>, JpaSpecificationExecutor<BlotterCase> {

    Optional<BlotterCase> findByBlotterNumber(String blotterNumber);

    boolean existsByBlotterNumber(String blotterNumber);

    List<BlotterCase> findAllByStatusAndDateFiledBefore(CaseStatus status, LocalDateTime threshold);
}
