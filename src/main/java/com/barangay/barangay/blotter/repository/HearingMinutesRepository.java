package com.barangay.barangay.blotter.repository;

import com.barangay.barangay.blotter.model.HearingMinutes;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface HearingMinutesRepository extends JpaRepository<HearingMinutes, Long> {

    boolean existsByHearingId(Long hearingId);

    Optional<HearingMinutes> findByHearingId(Long hearingId);
}