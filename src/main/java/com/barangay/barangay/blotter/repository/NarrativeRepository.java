package com.barangay.barangay.blotter.repository;

import com.barangay.barangay.blotter.model.Narrative;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface NarrativeRepository extends JpaRepository<Narrative, Long> {
}
