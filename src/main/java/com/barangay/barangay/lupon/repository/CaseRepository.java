package com.barangay.barangay.lupon.repository;

import com.barangay.barangay.blotter.model.BlotterCase;
import com.barangay.barangay.lupon.dto.LuponSummaryDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;

public interface CaseRepository extends JpaRepository<BlotterCase,Long> {


    @Query("""
    SELECT new com.barangay.barangay.lupon.dto.LuponSummaryDTO(
        b.id,
        b.blotterNumber,
        CONCAT(c.person.firstName, ' ', c.person.lastName),
        CONCAT(r.person.firstName, ' ', r.person.lastName),
        n.name,
        b.dateFiled,
        CAST(b.status AS string)
    )
    FROM BlotterCase b
    JOIN b.complainant c
    JOIN b.respondent r
    JOIN b.incidentDetail id
    JOIN id.natureOfComplaint n
    WHERE b.department.name = :deptName
    AND (CAST(:natureId AS Long) IS NULL OR n.id = :natureId)
    AND (CAST(:startDate AS LocalDateTime) IS NULL OR b.dateFiled >= :startDate)
    AND (CAST(:endDate AS LocalDateTime) IS NULL OR b.dateFiled <= :endDate)
    AND (CAST(:search AS String) IS NULL OR (
        b.blotterNumber ILIKE %:search% OR
        c.person.firstName ILIKE %:search% OR
        c.person.lastName ILIKE %:search% OR
        r.person.firstName ILIKE %:search% OR
        r.person.lastName ILIKE %:search%
    ))
    ORDER BY b.dateFiled DESC
""")
    Page<LuponSummaryDTO> findLuponSummaryWithFilters(
            @Param("deptName") String deptName,
            @Param("search") String search,
            @Param("natureId") Long natureId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            Pageable pageable);
}
