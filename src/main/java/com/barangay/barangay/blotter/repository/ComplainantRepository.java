package com.barangay.barangay.blotter.repository;

import com.barangay.barangay.blotter.model.Complainant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ComplainantRepository extends JpaRepository<Complainant, Long> {
}
