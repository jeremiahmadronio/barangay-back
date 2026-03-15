package com.barangay.barangay.blotter.repository;

import com.barangay.barangay.blotter.model.IncidentFrequency;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface IncidentFrequencyRepository extends JpaRepository<IncidentFrequency, Long> {
    Optional<IncidentFrequency> findByLabel(String label);

    Optional<IncidentFrequency> findByLabelIgnoreCase(String s);
}
