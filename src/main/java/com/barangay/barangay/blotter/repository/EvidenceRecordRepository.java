package com.barangay.barangay.blotter.repository;

import com.barangay.barangay.blotter.model.BlotterCase;
import com.barangay.barangay.blotter.model.EvidenceRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EvidenceRecordRepository extends JpaRepository<EvidenceRecord, Long> {
    List<EvidenceRecord> findAllByBlotterCase(BlotterCase blotterCase);

    List<EvidenceRecord> findByBlotterCaseId(Long caseId);
}
