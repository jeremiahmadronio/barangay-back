package com.barangay.barangay.blotter.repository;

import com.barangay.barangay.blotter.model.BlotterCase;
import com.barangay.barangay.enumerated.CaseStatus;
import com.barangay.barangay.enumerated.CaseType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public interface BlotterCaseRepository extends JpaRepository<BlotterCase, Long>, JpaSpecificationExecutor<BlotterCase> {

    Optional<BlotterCase> findByBlotterNumber(String blotterNumber);

    boolean existsByBlotterNumber(String blotterNumber);

    long countByCaseType(CaseType caseType);

    long countByCaseTypeAndStatusIn(CaseType caseType, Collection<CaseStatus> statuses);

    long countByCaseTypeAndStatus(CaseType caseType, CaseStatus status);


    List<BlotterCase> findAllByStatusAndDateFiledBefore(CaseStatus status, LocalDateTime threshold);
}
