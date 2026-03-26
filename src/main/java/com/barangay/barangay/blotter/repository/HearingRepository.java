package com.barangay.barangay.blotter.repository;

import com.barangay.barangay.blotter.model.BlotterCase;
import com.barangay.barangay.blotter.model.Hearing;
import com.barangay.barangay.enumerated.HearingStatus;
import com.barangay.barangay.lupon.dto.dashboard.UpcomingHearingDTO;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface HearingRepository extends JpaRepository<Hearing, Long> {


    @Query("SELECT h FROM Hearing h WHERE h.status = 'SCHEDULED' AND h.scheduledEnd < :now")
    List<Hearing> findOverdueHearings(@Param("now") LocalDateTime now);

    @Query("SELECT COUNT(h) > 0 FROM Hearing h " +
            "WHERE h.blotterCase.department.name = 'LUPONG_TAGAPAMAYAPA' " +
            "AND h.venue = :venue " +
            "AND h.status IN (com.barangay.barangay.enumerated.HearingStatus.SCHEDULED, " +
            "                 com.barangay.barangay.enumerated.HearingStatus.PENDING_MINUTES) " +
            "AND (:start < h.scheduledEnd AND :end > h.scheduledStart)")
    boolean existsActiveLuponOverlapping(
            @Param("venue") String venue,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );

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


    List<Hearing> findByBlotterCaseAndStatus(BlotterCase blotterCase, HearingStatus status);


    @Query("SELECT h FROM Hearing h " +
            "WHERE CAST(h.scheduledStart AS date) = :date " +
            "AND h.status != com.barangay.barangay.enumerated.HearingStatus.CANCELLED " +
            "AND UPPER(h.blotterCase.department.name) = 'BLOTTER'")
    List<Hearing> findActiveBlotterHearingsByDate(@Param("date") LocalDate date);

    @Query("SELECT h FROM Hearing h " +
            "WHERE h.scheduledStart BETWEEN :start AND :end " +
            "AND h.status != com.barangay.barangay.enumerated.HearingStatus.CANCELLED " +
            "AND UPPER(h.blotterCase.department.name) = 'BLOTTER'")
    List<Hearing> findAllActiveBlotterInMonth(
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );

    @Query("""
    SELECT COUNT(h.id) FROM Hearing h 
    WHERE h.blotterCase.department.name IN :deptNames 
    AND h.status = 'SCHEDULED' 
    AND h.scheduledStart >= :startOfDay 
    AND h.scheduledStart <= :endOfDay
""")
    Long countHearingsToday(
            @Param("deptNames") List<String> deptNames,
            @Param("startOfDay") LocalDateTime startOfDay,
            @Param("endOfDay") LocalDateTime endOfDay
    );

    @Query("SELECT new com.barangay.barangay.lupon.dto.dashboard.UpcomingHearingDTO(" +
            "h.id, " +
            "CONCAT(COALESCE(p1.firstName, 'N/A'), ' ', COALESCE(p1.lastName, ''), ' vs ', " +
            "COALESCE(p2.firstName, 'N/A'), ' ', COALESCE(p2.lastName, '')), " +
            "bc.blotterNumber, " +
            "h.scheduledStart) " +
            "FROM Hearing h " +
            "JOIN h.blotterCase bc " +
            "JOIN bc.department d " +
            "LEFT JOIN bc.complainant c " +
            "LEFT JOIN c.person p1 " +
            "LEFT JOIN bc.respondent r " +
            "LEFT JOIN r.person p2 " +
            "WHERE d.name IN :deptNames " +
            "AND h.scheduledStart >= :now " +
            "AND h.status = com.barangay.barangay.enumerated.HearingStatus.SCHEDULED " +
            "ORDER BY h.scheduledStart ASC")
    List<UpcomingHearingDTO> findUpcomingHearings(
            @Param("deptNames") List<String> deptNames,
            @Param("now") LocalDateTime now,
            Pageable pageable
    );
}


