package com.barangay.barangay.blotter.repository;

import com.barangay.barangay.blotter.model.Hearing;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface HearingRepository extends JpaRepository<Hearing, Long> {

    @Query("SELECT COUNT(h) > 0 FROM Hearing h WHERE h.venue = :venue " +
            "AND h.status = 'SCHEDULED' " +
            "AND h.scheduledEnd > CURRENT_TIMESTAMP " +
            "AND ((:start < h.scheduledEnd) AND (:end > h.scheduledStart))")
    boolean existsOverlapping(@Param("venue") String venue,
                              @Param("start") LocalDateTime start,
                              @Param("end") LocalDateTime end);

    // Get the next summon number for a specific case
    @Query("SELECT COALESCE(MAX(h.summonNumber), 0) FROM Hearing h WHERE h.blotterCase.id = :caseId")
    Short findLastSummonNumber(@Param("caseId") Long caseId);

    long countByBlotterCaseId(Long blotterCaseId);

    List<Hearing> findAllByBlotterCaseIdOrderByScheduledStartAsc(Long caseId);


    @Query("SELECT h FROM Hearing h JOIN h.blotterCase bc " +
            "WHERE CAST(h.scheduledStart AS date) = :date " +
            "AND bc.status NOT IN (com.barangay.barangay.enumerated.CaseStatus.SETTLED, " +
            "com.barangay.barangay.enumerated.CaseStatus.DISMISSED, " +
            "com.barangay.barangay.enumerated.CaseStatus.CERTIFIED_TO_FILE_ACTION) " +
            "ORDER BY h.scheduledStart ASC")
    List<Hearing> findActiveHearingsByDate(@Param("date") LocalDate date);

    @Query("SELECT h FROM Hearing h JOIN h.blotterCase bc " +
            "WHERE h.scheduledStart BETWEEN :start AND :end " +
            "AND bc.status NOT IN (com.barangay.barangay.enumerated.CaseStatus.SETTLED, " +
            "com.barangay.barangay.enumerated.CaseStatus.DISMISSED, " +
            "com.barangay.barangay.enumerated.CaseStatus.CERTIFIED_TO_FILE_ACTION)")
    List<Hearing> findAllActiveInMonth(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);



    @Query("SELECT h FROM Hearing h " +
            "JOIN FETCH h.blotterCase bc " +
            "LEFT JOIN FETCH bc.complainant " +
            "LEFT JOIN FETCH bc.respondent " +
            "WHERE h.id = :id")
    Optional<Hearing> findHearingForView(@Param("id") Long id);


}


