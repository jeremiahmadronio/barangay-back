package com.barangay.barangay.vawc.repository;

import com.barangay.barangay.vawc.model.Intervention;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface InterventionRepository extends JpaRepository<Intervention,Long> {
}
