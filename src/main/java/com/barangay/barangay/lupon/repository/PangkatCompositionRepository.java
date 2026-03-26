package com.barangay.barangay.lupon.repository;

import com.barangay.barangay.lupon.model.PangkatComposition;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PangkatCompositionRepository extends JpaRepository<PangkatComposition, Long> {
    List<PangkatComposition> findByBlotterCaseId(Long caseId);



}
