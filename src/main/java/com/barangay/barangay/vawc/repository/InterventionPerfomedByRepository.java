package com.barangay.barangay.vawc.repository;

import com.barangay.barangay.vawc.model.InterventionPerformedBy;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface InterventionPerfomedByRepository extends JpaRepository<InterventionPerformedBy, Long> {
}
