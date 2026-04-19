package com.barangay.barangay.blotter.repository;

import com.barangay.barangay.blotter.dto.hearing.FollowUpHearingDTO;
import com.barangay.barangay.blotter.model.HearingFollowUp;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FollowUpHearingRepository extends JpaRepository<HearingFollowUp, Long> {
}
