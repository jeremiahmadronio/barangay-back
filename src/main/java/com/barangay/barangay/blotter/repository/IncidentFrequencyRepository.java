package com.barangay.barangay.blotter.repository;

import com.barangay.barangay.blotter.dto.reports_and_display.IncidentFrequencyDTO;
import com.barangay.barangay.blotter.model.IncidentFrequency;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface IncidentFrequencyRepository extends JpaRepository<IncidentFrequency, Long> {
    Optional<IncidentFrequency> findByLabel(String label);

    Optional<IncidentFrequency> findByLabelIgnoreCase(String s);

    @Query("SELECT new com.barangay.barangay.blotter.dto.reports_and_display.IncidentFrequencyDTO(f.id, f.label) " +
            "FROM IncidentFrequency f ORDER BY f.id ASC")
    List<IncidentFrequencyDTO> getDropdownOptions();
}
