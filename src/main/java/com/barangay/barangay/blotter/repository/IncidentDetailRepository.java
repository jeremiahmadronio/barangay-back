package com.barangay.barangay.blotter.repository;

import com.barangay.barangay.blotter.model.IncidentDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface IncidentDetailRepository extends JpaRepository<IncidentDetail, Long> {
}
