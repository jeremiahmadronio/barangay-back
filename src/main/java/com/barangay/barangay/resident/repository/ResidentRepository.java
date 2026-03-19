package com.barangay.barangay.resident.repository;

import com.barangay.barangay.resident.dto.ResidentSummary;
import com.barangay.barangay.resident.model.People;
import com.barangay.barangay.resident.model.Resident;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ResidentRepository extends JpaRepository<Resident, Long> {

    boolean existsByBarangayIdNumber(String barangayIdNumber);


    @Query("SELECT new com.barangay.barangay.resident.dto.ResidentSummary(" +
            "r.residentId, r.barangayIdNumber, " +
            "CONCAT(r.person.firstName, ' ', r.person.lastName), " +
            "r.person.contactNumber, r.householdNumber, r.isVoter) " +
            "FROM Resident r " +
            "WHERE (:search IS NULL OR CAST(:search AS string) = '' " +
            "OR LOWER(CONCAT(r.person.firstName, ' ', r.person.lastName)) LIKE LOWER(CONCAT('%', CAST(:search AS string), '%')) " +
            "OR LOWER(r.barangayIdNumber) LIKE LOWER(CONCAT('%', CAST(:search AS string), '%'))) " +
            "AND (:gender IS NULL OR CAST(:gender AS string) = '' OR r.person.gender = CAST(:gender AS string)) " +
            "AND (:isVoter IS NULL OR r.isVoter = :isVoter) " +
            "AND (:household IS NULL OR CAST(:household AS string) = '' OR r.householdNumber LIKE CONCAT('%', CAST(:household AS string), '%'))")
    List<ResidentSummary> findWithFilters(
            @Param("search") String search,
            @Param("gender") String gender,
            @Param("isVoter") Boolean isVoter,
            @Param("household") String household

    );






}

