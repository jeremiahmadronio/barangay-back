package com.barangay.barangay.blotter.repository;

import com.barangay.barangay.blotter.model.CaseNote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CaseNoteRepository extends JpaRepository<CaseNote, Long> {

    List<CaseNote> findByBlotterCaseBlotterNumberOrderByCreatedAtDesc(String blotterNumber);
    List<CaseNote> findByBlotterCaseIdOrderByCreatedAtDesc(Long caseId);
}
