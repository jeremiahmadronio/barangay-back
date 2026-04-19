package com.barangay.barangay.vawc.repository;

import com.barangay.barangay.vawc.model.InterventionFollowUp;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InterventionFollowUpRepository extends JpaRepository<InterventionFollowUp,Integer> {

    List<InterventionFollowUp> findAllByInterventionIdOrderByCreatedAtDesc(Long interventionId);
}
