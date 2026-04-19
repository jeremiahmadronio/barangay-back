package com.barangay.barangay.blotter.repository;

import com.barangay.barangay.blotter.model.CaseTimeline;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CasteTimeLineRepository extends JpaRepository<CaseTimeline, Long> {

    List<CaseTimeline> findByBlotterCase_BlotterNumberOrderByEventDateDesc(String blotterNumber);

}
