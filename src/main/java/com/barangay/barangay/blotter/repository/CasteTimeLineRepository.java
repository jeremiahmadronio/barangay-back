package com.barangay.barangay.blotter.repository;

import com.barangay.barangay.blotter.model.CaseTimeline;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CasteTimeLineRepository extends JpaRepository<CaseTimeline, Long> {
}
