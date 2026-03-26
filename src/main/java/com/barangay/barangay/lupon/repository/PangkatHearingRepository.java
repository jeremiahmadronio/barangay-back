package com.barangay.barangay.lupon.repository;

import com.barangay.barangay.blotter.model.Hearing;
import com.barangay.barangay.enumerated.HearingStatus;
import com.barangay.barangay.lupon.dto.HearingScheduleDTO;
import com.barangay.barangay.lupon.dto.dashboard.UpcomingHearingDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface PangkatHearingRepository extends JpaRepository<Hearing, Long> {

    @Query("""
        SELECT new com.barangay.barangay.lupon.dto.HearingScheduleDTO(
            h.id,
            b.blotterNumber,
            h.createdAt,
            CONCAT(c.person.firstName, ' ', c.person.lastName),
            CONCAT(r.person.firstName, ' ', r.person.lastName),
            h.summonNumber,
            h.scheduledStart,
            h.scheduledEnd,
            h.status,
            h.notes,
            CONCAT(u.firstName, ' ', u.lastName),
            h.venue,
            b.status,
            hm.complainantPresent,
            hm.respondentPresent,
            hm.hearingNotes,
            CAST(hm.outcome AS string),
            CONCAT(rec.firstName, ' ', rec.lastName)
        )
        FROM Hearing h
        JOIN h.blotterCase b
        LEFT JOIN b.complainant c
        LEFT JOIN b.respondent r
        LEFT JOIN h.createdBy u
        LEFT JOIN HearingMinutes hm ON hm.hearing = h
        LEFT JOIN hm.recordedBy rec
        WHERE b.department.name = :deptName
        AND h.status IN :statuses
        AND (
            LOWER(b.blotterNumber) LIKE LOWER(:search) OR
            LOWER(c.person.firstName) LIKE LOWER(:search) OR
            LOWER(c.person.lastName) LIKE LOWER(:search) OR
            LOWER(r.person.firstName) LIKE LOWER(:search) OR
            LOWER(r.person.lastName) LIKE LOWER(:search)
        )
    """)
    Page<HearingScheduleDTO> findLuponHearingsWithFilters(
            @Param("deptName") String deptName,
            @Param("statuses") List<HearingStatus> statuses,
            @Param("search") String search,
            Pageable pageable
    );

    @Query("""
        SELECT COUNT(h.id) FROM Hearing h 
        WHERE h.blotterCase.department.name = :deptName 
        AND h.status = 'SCHEDULED' 
        AND h.scheduledStart >= :startOfDay 
        AND h.scheduledStart <= :endOfDay
    """)
    Long countHearingsToday(
            @Param("deptName") String deptName,
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
            "WHERE (UPPER(d.name) = UPPER(:deptName) OR REPLACE(UPPER(d.name), ' ', '_') = UPPER(:deptName)) " +
            "AND h.scheduledStart >= :now " +
            "AND h.status = com.barangay.barangay.enumerated.HearingStatus.SCHEDULED " +
            "ORDER BY h.scheduledStart ASC")
    List<UpcomingHearingDTO> findUpcomingHearings(
            @Param("deptName") String deptName,
            @Param("now") LocalDateTime now,
            Pageable pageable
    );

    List<Hearing> findAllByBlotterCaseBlotterNumberAndStatus(String blotterNumber, HearingStatus status);
}
