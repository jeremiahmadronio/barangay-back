package com.barangay.barangay.blotter.repository;

import com.barangay.barangay.blotter.model.BlotterCase;
import com.barangay.barangay.department.model.Department;
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

    // Count Formal Complaints by Department
    long countByCaseTypeAndDepartment(CaseType caseType, Department department);

    // Count by Status Group AND Department
    long countByCaseTypeAndStatusInAndDepartment(CaseType caseType, Collection<CaseStatus> statuses, Department department);

    // Count Specific Status AND Department
    long countByCaseTypeAndStatusAndDepartment(CaseType caseType, CaseStatus status, Department department);


    List<BlotterCase> findAllByStatusAndDateFiledBefore(CaseStatus status, LocalDateTime threshold);
}
